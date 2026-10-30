package com.aistudio.breakfree.recovr

import com.aistudio.breakfree.recovr.monitoring.BrowserChromeHeuristics
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the false positive where opening the browser's search box, or
 * sitting on the new-tab page, raised the explicit-content reminder because past
 * searches or "most visited" tiles contained an explicit word.
 *
 * The names below are the real resource-ids Chrome exposes (captured with
 * `uiautomator dump`), so a rename in Chrome shows up here rather than on a device.
 */
class BrowserChromeHeuristicsTest {

    // ── Browser chrome is never page content ─────────────────────────────────

    @Test
    fun `omnibox suggestion drop-down is chrome`() {
        assertTrue(
            BrowserChromeHeuristics.isChromeId(
                "com.android.chrome:id/omnibox_results_container"
            )
        )
        assertTrue(
            BrowserChromeHeuristics.isChromeId(
                "com.android.chrome:id/omnibox_suggestions_dropdown"
            )
        )
    }

    @Test
    fun `toolbar and address bar are chrome`() {
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/control_container")
        )
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/toolbar_container")
        )
        assertTrue(BrowserChromeHeuristics.isChromeId("com.android.chrome:id/url_bar"))
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/location_bar")
        )
    }

    @Test
    fun `new tab surface is chrome`() {
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/search_box_text")
        )
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/mv_tiles_container")
        )
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/tile_view_title")
        )
        assertTrue(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/feed_stream_recycler_view")
        )
    }

    @Test
    fun `page container and page nodes are not chrome`() {
        assertFalse(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/compositor_view_holder")
        )
        assertFalse(
            BrowserChromeHeuristics.isChromeId("com.android.chrome:id/coordinator")
        )
        // Rendered page text has no resource-id at all.
        assertFalse(BrowserChromeHeuristics.isChromeId(""))
        // The suggestion rows themselves carry a meaningless id; skipping the
        // container is what keeps them out of the scan.
        assertFalse(BrowserChromeHeuristics.isChromeId("com.android.chrome:id/line_1"))
    }

    @Test
    fun `address bar is recognised across browsers`() {
        assertTrue(BrowserChromeHeuristics.isAddressBarId("com.android.chrome:id/url_bar"))
        assertTrue(
            BrowserChromeHeuristics.isAddressBarId(
                "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
            )
        )
        assertTrue(
            BrowserChromeHeuristics.isAddressBarId(
                "com.sec.android.app.sbrowser:id/location_bar_edit_text"
            )
        )
        assertTrue(
            BrowserChromeHeuristics.isAddressBarId("com.android.chrome:id/search_box_text")
        )
    }

    // ── Only a loaded address may unlock content scanning ────────────────────

    @Test
    fun `new tab placeholder is not a loaded address`() {
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("Search Google or type URL"))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("Search or enter address"))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress(""))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("   "))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress(null))
    }

    @Test
    fun `a typed query is not a loaded address`() {
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("porn"))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("porn hub"))
    }

    @Test
    fun `browser internal pages are not loaded addresses`() {
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("chrome://history"))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("chrome://newtab"))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("about:blank"))
        assertFalse(BrowserChromeHeuristics.looksLikeLoadedAddress("edge://settings"))
    }

    @Test
    fun `loaded pages are recognised`() {
        assertTrue(
            BrowserChromeHeuristics.looksLikeLoadedAddress("10.0.2.2:8765/neutral.html")
        )
        assertTrue(BrowserChromeHeuristics.looksLikeLoadedAddress("example.com"))
        assertTrue(
            BrowserChromeHeuristics.looksLikeLoadedAddress(
                "google.com/search?q=panadol&oq=panadol"
            )
        )
        assertTrue(BrowserChromeHeuristics.looksLikeLoadedAddress("https://example.com/a"))
        assertTrue(BrowserChromeHeuristics.looksLikeLoadedAddress("localhost:3000"))
    }
}
