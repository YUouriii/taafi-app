package com.example.monitoring

object ExplicitContentDetector {

    // Common adult top domains and keywords
    private val explicitDomains = listOf(
        "pornhub", "xvideos", "xnxx", "redtube", "youporn",
        "chaturbate", "onlyfans", "stripchat", "cam4", "livejasmin",
        "spankbang", "eporner", "tube8", "xhamster", "brazzers",
        "naughtyamerica", "bangbros", "beeg", "rule34", "hentai",
        "javhd", "fuq.com", "heavy-r", "motherless", "tnaflix"
    )

    private val explicitKeywords = listOf(
        // English keywords
        "porn", "xxx", "sex video", "nude", "naked girls",
        "nsfw", "erotic video", "hardcore sex", "camgirl", "blowjob",
        "milf porn", "adult video", "free sex", "incest porn",
        // Arabic keywords
        "سكس", "بورنو", "أفلام إباحية", "مواقع إباحية", "صور عارية",
        "مقاطع جنس", "شيميل", "جنس مباشر", "عاهرات", "نيك", "مكالمات جنسية"
    )

    // Supported browser packages to monitor
    val monitoredBrowsers = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.sec.android.app.sbrowser", // Samsung Internet
        "com.microsoft.emmx", // Edge
        "com.opera.browser",
        "com.brave.browser",
        "com.duckduckgo.mobile.android"
    )

    fun isExplicitContent(text: String?): Pair<Boolean, String> {
        if (text.isNullOrBlank()) return Pair(false, "")
        val lower = text.lowercase().trim()

        // Check domains
        for (domain in explicitDomains) {
            if (lower.contains(domain)) {
                return Pair(true, domain)
            }
        }

        // Check keywords
        for (kw in explicitKeywords) {
            if (lower.contains(kw)) {
                return Pair(true, kw)
            }
        }

        return Pair(false, "")
    }

    fun getTotalBlockedRulesCount(): Int {
        return explicitDomains.size + explicitKeywords.size
    }
}
