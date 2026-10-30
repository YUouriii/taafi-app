package com.aistudio.breakfree.recovr.monitoring

import java.util.Locale

/**
 * Minimal IPv4/UDP/DNS codec for the on-device filter.
 *
 * The VPN only routes the resolver's own addresses into the tunnel, so every packet that
 * arrives here is a plain DNS query sent to that resolver. Nothing else has to be parsed,
 * forwarded or NATed — which is what keeps this small and auditable.
 *
 * Everything is written against raw byte arrays on purpose: the filter runs for every DNS
 * lookup on the device, so it allocates as little as possible and never touches a parser
 * that could be fed a malformed packet.
 */
object DnsPacket {

    private const val IP_HEADER_SIZE = 20
    private const val UDP_HEADER_SIZE = 8
    private const val DNS_HEADER_SIZE = 12
    private const val PROTOCOL_UDP = 17
    private const val MAX_DNS_NAME = 253
    private const val MAX_LABEL = 63

    /** A parsed DNS query, with everything needed to build the reply. */
    class Query(
        val id: Int,
        val host: String,
        val srcAddress: ByteArray,
        val srcPort: Int,
        val dstAddress: ByteArray,
        val dstPort: Int,
        val dnsOffset: Int,
        val dnsLength: Int,
        val questionEnd: Int
    ) {
        /** The question section, echoed unchanged in the answer. */
        fun question(packet: ByteArray): ByteArray =
            packet.copyOfRange(dnsOffset + DNS_HEADER_SIZE, questionEnd)

        /** The raw DNS message, forwarded to the resolver when the host is not blocked. */
        fun payload(packet: ByteArray): ByteArray =
            packet.copyOfRange(dnsOffset, dnsOffset + dnsLength)

        /** The request flags, so the reply can keep RD and the opcode. */
        fun flags(packet: ByteArray): Int = u16(packet, dnsOffset + 2)
    }

    /**
     * Parses an IPv4 UDP packet carrying a DNS query. Returns null for anything else
     * (IPv6, TCP, truncated packets, compressed or over-long names), which the caller
     * simply drops.
     */
    fun parseQuery(packet: ByteArray, length: Int): Query? {
        if (length < IP_HEADER_SIZE + UDP_HEADER_SIZE + DNS_HEADER_SIZE) return null

        val version = (packet[0].toInt() shr 4) and 0x0F
        if (version != 4) return null

        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (ihl < IP_HEADER_SIZE || ihl + UDP_HEADER_SIZE > length) return null

        if ((packet[9].toInt() and 0xFF) != PROTOCOL_UDP) return null

        val totalLength = u16(packet, 2)
        val end = if (totalLength in (ihl + UDP_HEADER_SIZE)..length) totalLength else length

        val srcPort = u16(packet, ihl)
        val dstPort = u16(packet, ihl + 2)
        if (dstPort != 53) return null

        val dnsOffset = ihl + UDP_HEADER_SIZE
        val dnsLength = end - dnsOffset
        if (dnsLength < DNS_HEADER_SIZE) return null

        val id = u16(packet, dnsOffset)
        val flags = u16(packet, dnsOffset + 2)
        if (flags and 0x8000 != 0) return null // a response, not a query
        if (u16(packet, dnsOffset + 4) < 1) return null // no question

        val questionEnd = readQuestionEnd(packet, dnsOffset + DNS_HEADER_SIZE, end) ?: return null
        val host = readName(packet, dnsOffset + DNS_HEADER_SIZE, end) ?: return null

        return Query(
            id = id,
            host = host,
            srcAddress = packet.copyOfRange(12, 16),
            srcPort = srcPort,
            dstAddress = packet.copyOfRange(16, 20),
            dstPort = dstPort,
            dnsOffset = dnsOffset,
            dnsLength = dnsLength,
            questionEnd = questionEnd
        )
    }

    /**
     * Wraps [dnsPayload] in an IPv4/UDP packet addressed back to the app that asked, with
     * the resolver as the source. The reply therefore looks exactly like one that came
     * from the resolver itself.
     */
    fun buildResponse(query: Query, dnsPayload: ByteArray): ByteArray {
        val total = IP_HEADER_SIZE + UDP_HEADER_SIZE + dnsPayload.size
        val packet = ByteArray(total)

        // IPv4 header
        packet[0] = 0x45                       // version 4, IHL 5
        packet[1] = 0                          // DSCP / ECN
        putU16(packet, 2, total)
        putU16(packet, 4, 0)                   // identification
        putU16(packet, 6, 0x4000)              // don't fragment
        packet[8] = 64                         // TTL
        packet[9] = PROTOCOL_UDP.toByte()
        putU16(packet, 10, 0)                  // checksum, filled below
        query.dstAddress.copyInto(packet, 12)  // source = resolver
        query.srcAddress.copyInto(packet, 16)  // destination = the app

        // UDP header
        val udpLength = UDP_HEADER_SIZE + dnsPayload.size
        putU16(packet, 20, query.dstPort)
        putU16(packet, 22, query.srcPort)
        putU16(packet, 24, udpLength)
        putU16(packet, 26, 0)
        dnsPayload.copyInto(packet, IP_HEADER_SIZE + UDP_HEADER_SIZE)

        putU16(packet, 10, ipChecksum(packet))
        putU16(packet, 26, udpChecksum(packet, udpLength))
        return packet
    }

    /**
     * Builds a "no such domain" answer. Used for hosts on the local blocklist: the browser
     * fails immediately instead of waiting for a timeout, and nothing is sent to the
     * resolver at all.
     */
    fun buildNxDomain(query: Query, request: ByteArray): ByteArray = buildError(query, request, 3)

    /** Builds an empty answer with [rcode] (2 = SERVFAIL), used when the resolver is unreachable. */
    fun buildError(query: Query, request: ByteArray, rcode: Int): ByteArray {
        val question = query.question(request)
        val payload = ByteArray(DNS_HEADER_SIZE + question.size)

        putU16(payload, 0, query.id)
        // QR = 1 (response), RD echoed, RA = 1, plus the given RCODE.
        putU16(payload, 2, 0x8000 or (query.flags(request) and 0x7900) or 0x0080 or (rcode and 0x0F))
        putU16(payload, 4, 1) // one question
        putU16(payload, 6, 0) // no answers
        putU16(payload, 8, 0) // no authority records
        putU16(payload, 10, 0) // no additional records
        question.copyInto(payload, DNS_HEADER_SIZE)

        return buildResponse(query, payload)
    }

    /**
     * Reads the DNS name at [offset], returning it in lower case, or null when the name is
     * malformed, compressed (compression is not legal in a question) or too long.
     */
    private fun readName(packet: ByteArray, offset: Int, end: Int): String? {
        var position = offset
        val labels = ArrayList<String>(4)
        var total = 0

        while (position < end) {
            val length = packet[position].toInt() and 0xFF
            if (length == 0) {
                if (labels.isEmpty()) return null
                return labels.joinToString(".").lowercase(Locale.ROOT)
            }
            // 0xC0 marks a compression pointer, which must not appear in a question.
            if (length and 0xC0 != 0) return null
            if (length > MAX_LABEL || position + 1 + length > end) return null

            total += length + 1
            if (total > MAX_DNS_NAME) return null

            labels.add(String(packet, position + 1, length, Charsets.US_ASCII))
            position += 1 + length
        }
        return null
    }

    /** Offset just past the question's QTYPE and QCLASS. */
    private fun readQuestionEnd(packet: ByteArray, offset: Int, end: Int): Int? {
        var position = offset
        while (position < end) {
            val length = packet[position].toInt() and 0xFF
            if (length == 0) {
                val questionEnd = position + 1 + 4
                return if (questionEnd <= end) questionEnd else null
            }
            if (length and 0xC0 != 0) return null
            position += 1 + length
        }
        return null
    }

    private fun ipChecksum(packet: ByteArray): Int {
        var sum = 0
        var i = 0
        while (i < IP_HEADER_SIZE) {
            sum += u16(packet, i)
            i += 2
        }
        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        return sum.inv() and 0xFFFF
    }

    /** UDP checksum over the pseudo-header, UDP header and payload. */
    private fun udpChecksum(packet: ByteArray, udpLength: Int): Int {
        var sum = 0
        var i = 12
        while (i < 20) { // source + destination addresses
            sum += u16(packet, i)
            i += 2
        }
        sum += PROTOCOL_UDP
        sum += udpLength

        i = IP_HEADER_SIZE
        val udpEnd = IP_HEADER_SIZE + udpLength
        while (i + 1 < udpEnd) {
            sum += u16(packet, i)
            i += 2
        }
        if (i < udpEnd) {
            sum += (packet[i].toInt() and 0xFF) shl 8
        }

        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        val checksum = sum.inv() and 0xFFFF
        // A computed checksum of zero is transmitted as 0xFFFF for UDP.
        return if (checksum == 0) 0xFFFF else checksum
    }

    private fun u16(packet: ByteArray, offset: Int): Int =
        ((packet[offset].toInt() and 0xFF) shl 8) or (packet[offset + 1].toInt() and 0xFF)

    private fun putU16(packet: ByteArray, offset: Int, value: Int) {
        packet[offset] = ((value shr 8) and 0xFF).toByte()
        packet[offset + 1] = (value and 0xFF).toByte()
    }
}
