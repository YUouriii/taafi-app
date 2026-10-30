package com.aistudio.breakfree.recovr.monitoring

import java.util.Locale

/**
 * Hostname matcher for the on-device DNS filter.
 *
 * Matching is by **whole label**, never by raw substring, so a false positive cannot
 * interrupt someone who is doing nothing wrong:
 *
 *  - `pornhub.com`, `www.pornhub.com` and `m.pornhub.co.uk` are blocked;
 *  - `notpornhub.example` and `pornhub-free-courses.org` are not, because the brand name
 *    is not a label of its own.
 *
 * The list is deliberately short. The resolver the VPN forwards to (Cloudflare for
 * Families) does the broad filtering; this list only guarantees that the obvious adult
 * brands fail immediately, even before the query leaves the phone.
 */
object DnsBlocklist {

    /** Labels that identify an adult brand. A host is blocked if any label matches. */
    private val blockedLabels = setOf(
        // Large tube sites
        "pornhub", "xvideos", "xnxx", "redtube", "youporn", "spankbang",
        "eporner", "tube8", "xhamster", "tnaflix", "drtuber", "hclips",
        "upornia", "txxx", "porntrex", "porn300", "pornhat", "hqporner",
        "ixxx", "vporn", "nuvid", "sexvid", "fuq", "beeg", "motherless",
        // Cams and subscription sites
        "chaturbate", "onlyfans", "stripchat", "cam4", "livejasmin",
        "bongacams", "camsoda", "myfreecams", "flirt4free", "jerkmate",
        "manyvids", "clips4sale", "faphouse", "xhamsterlive",
        // Studios and networks
        "brazzers", "naughtyamerica", "bangbros", "realitykings",
        "mofos", "twistys", "digitalplayground",
        // Comics, games, and dating
        "rule34", "hentai", "e621", "nhentai", "adultfriendfinder",
        "ashleymadison", "seeking",
        // Generic labels that only appear on adult domains
        "porn", "xxx"
    )

    /**
     * True when [host] (a DNS name such as `www.pornhub.com`) belongs to a blocked brand.
     * `null`, empty and single-label hosts (e.g. `localhost`) are never blocked.
     */
    fun isBlocked(host: String?): Boolean {
        val normalized = normalize(host) ?: return false
        val labels = normalized.split('.')
        if (labels.size < 2) return false
        return labels.any { it in blockedLabels }
    }

    /** Number of distinct brands covered, shown in the UI. */
    val brandCount: Int get() = blockedLabels.size

    /**
     * Lower-cases, strips the trailing root dot, and rejects anything that is not a plain
     * DNS name. Returning null keeps malformed or hostile names out of the matcher.
     */
    private fun normalize(host: String?): String? {
        val value = host?.trim()?.trimEnd('.')?.lowercase(Locale.ROOT) ?: return null
        if (value.isEmpty() || value.length > 253) return null
        if (!value.all { it.isLetterOrDigit() || it == '.' || it == '-' || it == '_' }) return null
        return value
    }
}
