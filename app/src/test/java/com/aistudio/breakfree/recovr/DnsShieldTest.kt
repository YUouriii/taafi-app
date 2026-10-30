package com.aistudio.breakfree.recovr

import com.aistudio.breakfree.recovr.monitoring.DnsBlocklist
import com.aistudio.breakfree.recovr.monitoring.DnsPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The DNS layer decides, for every lookup on the phone, whether a name is blocked. A false
 * positive breaks an innocent site; a false negative lets through exactly what the shield
 * exists to stop. Both sides are pinned here, together with the packet codec, so the
 * checksums and header rewrites are verified without a device.
 */
class DnsBlocklistTest {

    @Test
    fun `blocks an adult brand as its own label`() {
        assertTrue(DnsBlocklist.isBlocked("pornhub.com"))
        assertTrue(DnsBlocklist.isBlocked("www.xvideos.com"))
        assertTrue(DnsBlocklist.isBlocked("m.pornhub.co.uk"))
        assertTrue(DnsBlocklist.isBlocked("ONLYFANS.COM"))
    }

    @Test
    fun `does not block a brand that is only part of a longer label`() {
        assertFalse(DnsBlocklist.isBlocked("notpornhub.example"))
        assertFalse(DnsBlocklist.isBlocked("pornhub-free-courses.org"))
        assertFalse(DnsBlocklist.isBlocked("myonlyfansreview.com"))
    }

    @Test
    fun `does not block ordinary sites`() {
        assertFalse(DnsBlocklist.isBlocked("google.com"))
        assertFalse(DnsBlocklist.isBlocked("youtube.com"))
        assertFalse(DnsBlocklist.isBlocked("wikipedia.org"))
    }

    @Test
    fun `ignores single label and malformed hosts`() {
        assertFalse(DnsBlocklist.isBlocked("localhost"))
        assertFalse(DnsBlocklist.isBlocked(""))
        assertFalse(DnsBlocklist.isBlocked(null))
        assertFalse(DnsBlocklist.isBlocked("porn hub.com"))
    }

    @Test
    fun `tolerates a trailing root dot`() {
        assertTrue(DnsBlocklist.isBlocked("pornhub.com."))
    }
}

class DnsPacketTest {

    private val clientAddress = byteArrayOf(10, 111, 222.toByte(), 1)
    private val resolverAddress = byteArrayOf(1, 1, 1, 3)

    @Test
    fun `parses the question of an IPv4 UDP query`() {
        val packet = query("www.pornhub.com", id = 0x1234)

        val query = DnsPacket.parseQuery(packet, packet.size)

        assertNotNull(query)
        assertEquals(0x1234, query!!.id)
        assertEquals("www.pornhub.com", query.host)
        assertEquals(40000, query.srcPort)
        assertEquals(53, query.dstPort)
        assertTrue(query.srcAddress.contentEquals(clientAddress))
        assertTrue(query.dstAddress.contentEquals(resolverAddress))
    }

    @Test
    fun `ignores responses and non DNS ports`() {
        val packet = query("example.com")
        packet[30] = 0x81.toByte() // QR = 1 in the DNS flags (offset 28 + 2)
        assertNull(DnsPacket.parseQuery(packet, packet.size))

        val other = query("example.com")
        other[22] = 0
        other[23] = 80 // destination port 80, not 53
        assertNull(DnsPacket.parseQuery(other, other.size))
    }

    @Test
    fun `nxdomain answer swaps the addresses and is a valid packet`() {
        val request = query("pornhub.com", id = 0x4321)
        val parsed = DnsPacket.parseQuery(request, request.size)!!
        val answer = DnsPacket.buildNxDomain(parsed, request)

        // IPv4 / UDP
        assertEquals(4, (answer[0].toInt() shr 4) and 0x0F)
        assertEquals(17, answer[9].toInt() and 0xFF)
        assertEquals(answer.size, u16(answer, 2))

        // Source is the resolver, destination is the app that asked.
        assertTrue(answer.copyOfRange(12, 16).contentEquals(resolverAddress))
        assertTrue(answer.copyOfRange(16, 20).contentEquals(clientAddress))

        // Ports are swapped.
        assertEquals(53, u16(answer, 20))
        assertEquals(40000, u16(answer, 22))

        // Both checksums verify.
        assertEquals(0xFFFF, checksum(answer, 0, 20))
        assertEquals(0xFFFF, udpChecksum(answer))

        // QR = 1, RD preserved, RCODE = 3 (NXDOMAIN), one question, no answers.
        val dnsOffset = 20 + 8
        assertEquals(0x4321, u16(answer, dnsOffset))
        val flags = u16(answer, dnsOffset + 2)
        assertEquals(0x8000, flags and 0x8000)
        assertEquals(0x0100, flags and 0x0100)
        assertEquals(3, flags and 0x0F)
        assertEquals(1, u16(answer, dnsOffset + 4))
        assertEquals(0, u16(answer, dnsOffset + 6))

        // The question is echoed, so the resolver's name matches what was asked.
        assertEquals(
            "pornhub.com",
            readName(answer, dnsOffset + 12)
        )
    }

    @Test
    fun `relayed answer keeps the resolver payload`() {
        val request = query("example.com")
        val parsed = DnsPacket.parseQuery(request, request.size)!!
        val upstream = DnsPacket.buildNxDomain(parsed, request)

        // Take the DNS message out of the synthetic reply and wrap it as if it had come
        // from the network: the codec must produce the same wire shape either way.
        val payload = upstream.copyOfRange(28, upstream.size)
        val rebuilt = DnsPacket.buildResponse(parsed, payload)

        assertEquals(upstream.size, rebuilt.size)
        assertTrue(upstream.contentEquals(rebuilt))
    }

    @Test
    fun `servfail answer uses rcode 2`() {
        val request = query("example.com")
        val parsed = DnsPacket.parseQuery(request, request.size)!!
        val answer = DnsPacket.buildError(parsed, request, 2)

        val flags = u16(answer, 28 + 2)
        assertEquals(2, flags and 0x0F)
        assertEquals(1, u16(answer, 28 + 4))
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    /** Builds a minimal IPv4 + UDP + DNS query, as the tunnel would receive it. */
    private fun query(host: String, id: Int = 0x1234): ByteArray {
        val labels = host.split(".")
        val nameSize = labels.sumOf { it.length + 1 } + 1
        val name = ByteArray(nameSize)
        var cursor = 0
        for (label in labels) {
            name[cursor++] = label.length.toByte()
            label.toByteArray(Charsets.US_ASCII).copyInto(name, cursor)
            cursor += label.length
        }
        name[cursor] = 0

        val dns = ByteArray(12 + nameSize + 4)
        dns[0] = (id shr 8).toByte()
        dns[1] = id.toByte()
        dns[2] = 0x01 // RD
        dns[3] = 0x00
        dns[4] = 0x00
        dns[5] = 0x01 // one question
        name.copyInto(dns, 12)
        dns[12 + nameSize + 1] = 0x01 // QTYPE A
        dns[12 + nameSize + 3] = 0x01 // QCLASS IN

        val total = 20 + 8 + dns.size
        val packet = ByteArray(total)
        packet[0] = 0x45
        putU16(packet, 2, total)
        packet[8] = 64
        packet[9] = 17
        clientAddress.copyInto(packet, 12)
        resolverAddress.copyInto(packet, 16)
        putU16(packet, 20, 40000)
        putU16(packet, 22, 53)
        putU16(packet, 24, 8 + dns.size)
        dns.copyInto(packet, 28)
        return packet
    }

    private fun readName(packet: ByteArray, offset: Int): String {
        val labels = mutableListOf<String>()
        var position = offset
        while (true) {
            val length = packet[position].toInt() and 0xFF
            if (length == 0) break
            labels.add(String(packet, position + 1, length, Charsets.US_ASCII))
            position += 1 + length
        }
        return labels.joinToString(".")
    }

    private fun checksum(packet: ByteArray, from: Int, to: Int): Int {
        var sum = 0
        var i = from
        while (i < to) {
            sum += u16(packet, i)
            i += 2
        }
        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        return sum and 0xFFFF
    }

    /** Recomputes the UDP checksum with the stored value zeroed, then compares. */
    private fun udpChecksum(packet: ByteArray): Int {
        val stored = u16(packet, 26)
        val copy = packet.copyOf()
        putU16(copy, 26, 0)

        var sum = 0
        var i = 12
        while (i < 20) {
            sum += u16(copy, i)
            i += 2
        }
        sum += 17
        sum += copy.size - 20

        i = 20
        while (i + 1 < copy.size) {
            sum += u16(copy, i)
            i += 2
        }
        if (i < copy.size) sum += (copy[i].toInt() and 0xFF) shl 8

        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        var expected = sum.inv() and 0xFFFF
        if (expected == 0) expected = 0xFFFF
        return if (stored == expected) 0xFFFF else stored
    }

    private fun u16(packet: ByteArray, offset: Int): Int =
        ((packet[offset].toInt() and 0xFF) shl 8) or (packet[offset + 1].toInt() and 0xFF)

    private fun putU16(packet: ByteArray, offset: Int, value: Int) {
        packet[offset] = ((value shr 8) and 0xFF).toByte()
        packet[offset + 1] = (value and 0xFF).toByte()
    }
}
