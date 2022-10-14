/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android

import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.webextension.DisabledFlags
import mozilla.components.concept.engine.webextension.Metadata
import mozilla.components.concept.engine.webextension.WebExtension
import mozilla.components.feature.addons.migration.DefaultSupportedAddonsChecker
import mozilla.components.service.glean.testing.GleanTestRule
import mozilla.components.support.test.robolectric.testContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import net.waterfox.android.GleanMetrics.Addons
import net.waterfox.android.GleanMetrics.Metrics
import net.waterfox.android.GleanMetrics.Preferences
import net.waterfox.android.GleanMetrics.SearchDefaultEngine
import net.waterfox.android.GleanMetrics.TopSites
import net.waterfox.android.components.metrics.MozillaProductDetector
import net.waterfox.android.components.toolbar.ToolbarPosition
import net.waterfox.android.helpers.WaterfoxRobolectricTestRunner
import net.waterfox.android.utils.BrowsersCache
import net.waterfox.android.utils.Settings
import org.robolectric.annotation.Config

@RunWith(WaterfoxRobolectricTestRunner::class)
class WaterfoxApplicationTest {

    @get:Rule val gleanTestRule = GleanTestRule(ApplicationProvider.getApplicationContext())

    private lateinit var application: WaterfoxApplication
    private lateinit var browsersCache: BrowsersCache
    private lateinit var mozillaProductDetector: MozillaProductDetector
    private lateinit var browserStore: BrowserStore

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        browsersCache = mockk(relaxed = true)
        mozillaProductDetector = mockk(relaxed = true)
        browserStore = BrowserStore()
    }

    @Test
    fun `GIVEN there are unsupported addons installed WHEN subscribing for new add-ons checks THEN register for checks`() {
        val checker = mockk<DefaultSupportedAddonsChecker>(relaxed = true)
        val unSupportedExtension: WebExtension = mockk()
        val metadata: Metadata = mockk()

        every { unSupportedExtension.getMetadata() } returns metadata
        every { metadata.disabledFlags } returns DisabledFlags.select(DisabledFlags.APP_SUPPORT)

        application.subscribeForNewAddonsIfNeeded(checker, listOf(unSupportedExtension))

        verify { checker.registerForChecks() }
    }

    @Test
    fun `GIVEN there are no unsupported addons installed WHEN subscribing for new add-ons checks THEN unregister for checks`() {
        val checker = mockk<DefaultSupportedAddonsChecker>(relaxed = true)
        val unSupportedExtension: WebExtension = mockk()
        val metadata: Metadata = mockk()

        every { unSupportedExtension.getMetadata() } returns metadata
        every { metadata.disabledFlags } returns DisabledFlags.select(DisabledFlags.USER)

        application.subscribeForNewAddonsIfNeeded(checker, listOf(unSupportedExtension))

        verify { checker.unregisterForChecks() }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `WHEN setStartupMetrics is called THEN sets some base metrics`() {
        val expectedAppName = "net.waterfox.android"
        val expectedAppInstallSource = "org.mozilla.install.source"
        val settings = spyk(Settings(testContext))
        val application = spyk(application)
        val packageManager: PackageManager = mockk()

        every { application.packageManager } returns packageManager
        @Suppress("DEPRECATION")
        every { packageManager.getInstallerPackageName(any()) } returns expectedAppInstallSource
        every { browsersCache.all(any()).isDefaultBrowser } returns true
        every { mozillaProductDetector.getMozillaBrowserDefault(any()) } returns expectedAppName
        every { mozillaProductDetector.getInstalledMozillaProducts(any()) } returns listOf(expectedAppName)
        every { settings.searchWidgetInstalled } returns true
        every { settings.openTabsCount } returns 1
        every { settings.topSitesSize } returns 2
        every { settings.installedAddonsCount } returns 3
        every { settings.installedAddonsList } returns "test1,test2,test3"
        every { settings.enabledAddonsCount } returns 2
        every { settings.enabledAddonsList } returns "test1,test2"
        every { settings.desktopBookmarksSize } returns 4
        every { settings.mobileBookmarksSize } returns 5
        every { settings.toolbarPosition } returns ToolbarPosition.BOTTOM
        every { settings.getTabViewPingString() } returns "test"
        every { settings.getTabTimeoutPingString() } returns "test"
        every { settings.shouldShowSearchSuggestions } returns true
        every { settings.shouldUseTrackingProtection } returns true
        every { settings.isRemoteDebuggingEnabled } returns true
        every { settings.isTelemetryEnabled } returns true
        every { settings.isExperimentationEnabled } returns true
        every { settings.shouldShowHistorySuggestions } returns true
        every { settings.shouldShowBookmarkSuggestions } returns true
        every { settings.shouldShowClipboardSuggestions } returns true
        every { settings.shouldShowSearchShortcuts } returns true
        every { settings.openLinksInAPrivateTab } returns true
        every { settings.shouldShowSearchSuggestionsInPrivate } returns true
        every { settings.shouldShowVoiceSearch } returns true
        every { settings.openLinksInExternalApp } returns true
        every { settings.shouldUseFixedTopToolbar } returns true
        every { settings.useStandardTrackingProtection } returns true
        every { settings.switchServiceIsEnabled } returns true
        every { settings.touchExplorationIsEnabled } returns true
        every { settings.shouldUseLightTheme } returns true
        every { settings.signedInFxaAccount } returns true
        every { settings.showRecentTabsFeature } returns true
        every { settings.showRecentBookmarksFeature } returns true
        every { settings.showTopSitesFeature } returns true
        every { settings.historyMetadataUIFeature } returns true
        every { settings.showPocketRecommendationsFeature } returns true
        every { settings.showContileFeature } returns true
        every { settings.searchTermTabGroupsAreEnabled } returns true
        every { application.reportHomeScreenMetrics(settings) } just Runs
        every { settings.inactiveTabsAreEnabled } returns true

        assertTrue(settings.contileContextId.isEmpty())
        assertNull(TopSites.contextId.testGetValue())

        application.setStartupMetrics(browserStore, settings, browsersCache, mozillaProductDetector)

        // Verify that browser defaults metrics are set.
        assertEquals("Mozilla", Metrics.distributionId.testGetValue())
        assertEquals(true, Metrics.defaultBrowser.testGetValue())
        assertEquals(expectedAppName, Metrics.defaultMozBrowser.testGetValue())
        assertEquals(listOf(expectedAppName), Metrics.mozillaProducts.testGetValue())
        assertEquals(true, Metrics.searchWidgetInstalled.testGetValue())
        assertEquals(true, Metrics.hasOpenTabs.testGetValue())
        assertEquals(1, Metrics.tabsOpenCount.testGetValue())
        assertEquals(true, Metrics.hasTopSites.testGetValue())
        assertEquals(2, Metrics.topSitesCount.testGetValue())
        assertEquals(true, Addons.hasInstalledAddons.testGetValue())
        assertEquals(listOf("test1", "test2", "test3"), Addons.installedAddons.testGetValue())
        assertEquals(true, Addons.hasEnabledAddons.testGetValue())
        assertEquals(listOf("test1", "test2"), Addons.enabledAddons.testGetValue())
        assertEquals(true, Preferences.searchSuggestionsEnabled.testGetValue())
        assertEquals(true, Preferences.remoteDebuggingEnabled.testGetValue())
        assertEquals(true, Preferences.telemetryEnabled.testGetValue())
        assertEquals(true, Preferences.studiesEnabled.testGetValue())
        assertEquals(true, Preferences.browsingHistorySuggestion.testGetValue())
        assertEquals(true, Preferences.bookmarksSuggestion.testGetValue())
        assertEquals(true, Preferences.clipboardSuggestionsEnabled.testGetValue())
        assertEquals(true, Preferences.searchShortcutsEnabled.testGetValue())
        assertEquals(true, Preferences.voiceSearchEnabled.testGetValue())
        assertEquals(true, Preferences.openLinksInAppEnabled.testGetValue())
        assertEquals(true, Preferences.signedInSync.testGetValue())
        assertEquals(true, Preferences.searchTermGroupsEnabled.testGetValue())
        assertEquals(emptyList<String>(), Preferences.syncItems.testGetValue())
        assertEquals("fixed_top", Preferences.toolbarPositionSetting.testGetValue())
        assertEquals("standard", Preferences.enhancedTrackingProtection.testGetValue())
        assertEquals(listOf("switch", "touch exploration"), Preferences.accessibilityServices.testGetValue())
        assertEquals(true, Preferences.inactiveTabsEnabled.testGetValue())
        assertEquals(expectedAppInstallSource, Metrics.installSource.testGetValue())
        assertEquals(true, Metrics.defaultWallpaper.testGetValue())

        val contextId = TopSites.contextId.testGetValue()!!.toString()

        assertNotNull(TopSites.contextId.testGetValue())
        assertEquals(contextId, settings.contileContextId)

        // Verify that search engine defaults are NOT set. This test does
        // not mock most of the objects telemetry is collected from.
        assertNull(SearchDefaultEngine.code.testGetValue())
        assertNull(SearchDefaultEngine.name.testGetValue())
        assertNull(SearchDefaultEngine.searchUrl.testGetValue())

        application.setStartupMetrics(browserStore, settings, browsersCache, mozillaProductDetector)

        assertEquals(contextId, TopSites.contextId.testGetValue()!!.toString())
        assertEquals(contextId, settings.contileContextId)
    }
}
