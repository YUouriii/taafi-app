package com.aistudio.breakfree.recovr.monitoring

import java.util.Locale

/**
 * On-device explicit-content matcher.
 *
 * Matching rules are deliberately conservative, because a false positive means the app
 * interrupts someone who is doing nothing wrong:
 *
 *  - Domains are matched against **host labels**, never raw substrings, so
 *    `notpornhub.example` is not treated as `pornhub`.
 *  - Keywords are matched as **whole words or phrases**, so "denuded" does not match
 *    "nude" and "نيكولاس" does not match "نيك".
 *  - Arabic keywords additionally tolerate the common attached prefixes
 *    (و / ال / بال / لل …), because Arabic glues them onto the word.
 *
 * Nothing here leaves the device.
 */
object ExplicitContentDetector {

    private val NO_MATCH = Pair(false, "")

    /** Adult site brand names, matched against individual host labels. */
    private val explicitDomains = listOf(
        "pornhub", "xvideos", "xnxx", "redtube", "youporn",
        "chaturbate", "onlyfans", "stripchat", "cam4", "livejasmin",
        "spankbang", "eporner", "tube8", "xhamster", "brazzers",
        "naughtyamerica", "bangbros", "beeg", "rule34", "hentai",
        "javhd", "fuq", "heavy-r", "motherless", "tnaflix"
    )

    /** Words and phrases that describe explicit intent in a search or a page title. */
    private val explicitKeywords = listOf(
        // English
        "porn", "pornography", "xxx", "sex video", "nude", "naked girls",
        "nsfw", "erotic video", "hardcore sex", "camgirl", "blowjob",
        "milf porn", "adult video", "free sex", "incest porn",
        // Arabic
        "سكس", "بورنو", "أفلام إباحية", "مواقع إباحية", "صور عارية",
        "مقاطع جنس", "شيميل", "جنس مباشر", "عاهرات", "نيك", "مكالمات جنسية"
    )

    /** Prefixes Arabic attaches to a word (and / the / with the …). */
    private val arabicPrefixes = setOf(
        "و", "ف", "ب", "ل", "ك",
        "ال", "وال", "فال", "بال", "كال", "لل", "ولل"
    )

    private val nonWord = Regex("[^\\p{L}\\p{N}]+")
    private val arabicDiacritics = Regex("[\\u064B-\\u0652\\u0670\\u0640]")
    private val hostPattern = Regex("^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")

    // Cheap pre-filter: if none of these appear, the text cannot match and we skip all
    // tokenising. Keeps the accessibility-tree walk inexpensive.
    private val anchors: List<String> =
        (explicitDomains + explicitKeywords)
            .map { normalize(it).take(3) }
            .filter { it.length == 3 }
            .distinct()

    private val normalizedKeywords: List<String> = explicitKeywords.map { normalize(it) }

    /** Supported browser packages to monitor. */
    val monitoredBrowsers = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.sec.android.app.sbrowser", // Samsung Internet
        "com.microsoft.emmx", // Edge
        "com.opera.browser",
        "com.brave.browser",
        "com.duckduckgo.mobile.android"
    )

    /**
     * Social and video apps whose **visible text** is monitored once the user opts in.
     *
     * Text only: accessibility cannot see thumbnails, photos or video frames, so a post
     * that is purely an image is out of reach. Titles, captions, tweets, post bodies and
     * descriptions are covered.
     */
    val monitoredApps = setOf(
        "com.google.android.youtube",
        "com.twitter.android", // X
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok (global)
        "com.ss.android.ugc.trill", // TikTok (some regions)
        "com.facebook.katana",
        "com.facebook.lite",
        "com.reddit.frontpage"
    )


    /**
     * @param text anything the user can see: a URL, an address-bar string, a page title
     *             or a search query.
     * @param includeKeywords when false only the domain rules run (used when the user
     *             turns off keyword filtering in Settings).
     * @return `true` plus the rule that matched, or `false` with an empty string.
     */
    fun isExplicitContent(text: String?, includeKeywords: Boolean = true): Pair<Boolean, String> {
        if (text.isNullOrBlank()) return NO_MATCH
        val normalized = normalize(text)

        // Fast bail-out before any regex work.
        if (anchors.none { normalized.contains(it) }) return NO_MATCH

        matchDomain(normalized)?.let { return Pair(true, it) }

        if (includeKeywords) {
            matchKeyword(normalized)?.let { return Pair(true, it) }
        }

        return NO_MATCH
    }

    /** Only the domain rules — used for URL/address-bar checks. */
    fun isExplicitDomain(text: String?): Pair<Boolean, String> =
        isExplicitContent(text, includeKeywords = false)

    private fun matchDomain(normalized: String): String? {
        val host = extractHost(normalized) ?: return null
        val labels = host.split('.')
        for (domain in explicitDomains) {
            if (labels.any { it == domain }) return domain
        }
        return null
    }

    private fun matchKeyword(normalized: String): String? {
        val squashed = squash(normalized)
        if (squashed.isEmpty()) return null
        val tokens = squashed.split(' ')

        for (keyword in normalizedKeywords) {
            if (keyword.contains(' ')) {
                if (containsPhrase(squashed, keyword)) return keyword
                continue
            }
            if (tokens.any { tokenMatches(it, keyword) }) return keyword
        }
        return null
    }

    private fun tokenMatches(token: String, keyword: String): Boolean {
        if (token == keyword) return true
        // Arabic glues short prefixes onto the word: والسكس == و + السكس.
        if (token.length <= keyword.length) return false
        if (!token.endsWith(keyword)) return false
        val prefix = token.dropLast(keyword.length)
        return prefix in arabicPrefixes
    }

    /**
     * Matches a multi-word keyword against the squashed text. Every word may carry the
     * usual Arabic prefix, because definiteness is repeated on each word of a phrase
     * ("المواقع الإباحية" still matches "مواقع إباحية").
     */
    private fun containsPhrase(squashed: String, phrase: String): Boolean {
        val phraseTokens = phrase.split(' ')
        val tokens = squashed.split(' ')
        if (phraseTokens.isEmpty() || phraseTokens.size > tokens.size) return false

        for (start in 0..(tokens.size - phraseTokens.size)) {
            var matched = true
            for (offset in phraseTokens.indices) {
                if (!tokenMatches(tokens[start + offset], phraseTokens[offset])) {
                    matched = false
                    break
                }
            }
            if (matched) return true
        }
        return false
    }

    /**
     * Pulls a host out of a URL, an address-bar string or a bare domain.
     * Returns null when the text does not look like a host at all (e.g. a search query).
     */
    private fun extractHost(normalized: String): String? {
        var candidate = normalized
            .substringAfter("://", normalized)
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore(' ')
            .trim()
        if (candidate.startsWith("www.")) candidate = candidate.removePrefix("www.")
        candidate = candidate.substringBefore(':') // strip port
        if (!candidate.contains('.')) return null
        return if (hostPattern.matches(candidate)) candidate else null
    }

    /** Lowercase, fold Arabic letter variants, drop diacritics and tatweel. */
    private fun normalize(text: String): String {
        var out = text.lowercase(Locale.ROOT)
        out = arabicDiacritics.replace(out, "")
        out = out
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
        return out
    }

    /** Collapse every run of non-letter characters into a single space. */
    private fun squash(normalized: String): String =
        nonWord.replace(normalized, " ").trim()

    fun getTotalBlockedRulesCount(): Int {
        return explicitDomains.size + explicitKeywords.size
    }
}
