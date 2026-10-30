package com.aistudio.breakfree.recovr.monitoring

import java.util.Locale

/**
 * Name and shape rules used to tell **browser chrome** from **page content** in an
 * accessibility tree.
 *
 * They live outside the service so they can be unit-tested: the false positive they
 * prevent (reading search-history suggestions or new-tab tiles as if they were the page
 * the user is viewing) is easy to reintroduce, and hard to notice on a device.
 */
internal object BrowserChromeHeuristics {

    /**
     * Browser-internal screens (settings, history, new tab). They render the user's own
     * data rather than a web page, so their text must never trigger a reminder.
     */
    private val internalPagePrefixes = listOf(
        "chrome://",
        "chrome-native://",
        "chrome-untrusted://",
        "chrome-extension://",
        "moz-extension://",
        "about:",
        "edge://",
        "brave://",
        "opera://",
        "vivaldi://",
        "view-source:",
        "javascript:",
        "data:"
    )

    /**
     * Resource-ID fragments that belong to browser chrome, never to page content.
     * Matched against the container id so a whole subtree can be skipped.
     */
    private val chromeIdFragments = listOf(
        "control_container",
        "toolbar",
        "location_bar",
        "url_bar",
        "omnibox",
        "search_box",
        "tab_switcher",
        "tabswitcher",
        "tab_strip",
        "find_in_page",
        "feed_stream",
        "mv_tiles",
        "tile_view",
        "composeplate",
        "home_button",
        "menu_button"
    )

    /** The node that holds the address (Chrome: `url_bar`, Firefox: `…_url_view`). */
    fun isAddressBarId(viewId: String): Boolean {
        val lower = viewId.lowercase(Locale.ROOT)
        return lower.contains("url") ||
            lower.contains("omnibox") ||
            lower.contains("location_bar") ||
            lower.contains("search_box") ||
            lower.contains("toolbar_url")
    }

    /**
     * Suggestion/history/autocomplete containers. The drop-down exists only while the
     * omnibox is active, so its presence is itself a signal of "search mode".
     */
    fun isSuggestionAreaId(viewId: String): Boolean {
        val lower = viewId.lowercase(Locale.ROOT)
        return lower.contains("suggestion") ||
            lower.contains("dropdown") ||
            lower.contains("autocomplete") ||
            lower.contains("history") ||
            lower.contains("omnibox_results") ||
            lower.contains("search_suggestion")
    }

    /** Toolbar, omnibox, drop-down, tab strip and new-tab surface. */
    fun isChromeId(viewId: String): Boolean {
        val lower = viewId.lowercase(Locale.ROOT)
        if (isSuggestionAreaId(lower)) return true
        return chromeIdFragments.any { lower.contains(it) }
    }

    /**
     * True when the address bar holds a real, loaded web address rather than a
     * placeholder or an internal browser page.
     *
     * Chrome shows "Search Google or type URL" on a new-tab page; other browsers use
     * similar phrases. None of them contain a host, while every loaded address does
     * (`example.com`, `10.0.2.2:8765/page`, `localhost:3000`).
     */
    fun looksLikeLoadedAddress(text: String?): Boolean {
        val value = text?.trim().orEmpty()
        if (value.isEmpty()) return false

        val lower = value.lowercase(Locale.ROOT)
        if (internalPagePrefixes.any { lower.startsWith(it) }) return false

        return lower.contains("://") ||
            lower.contains('.') ||
            lower.startsWith("localhost")
    }
}
