/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android.home

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.support.test.robolectric.testContext
import net.waterfox.android.BrowserDirection
import net.waterfox.android.HomeActivity
import net.waterfox.android.R
import net.waterfox.android.components.accounts.AccountState
import net.waterfox.android.components.accounts.WaterfoxFxAEntryPoint
import net.waterfox.android.ext.nav
import net.waterfox.android.ext.settings
import net.waterfox.android.helpers.WaterfoxRobolectricTestRunner
import net.waterfox.android.settings.SupportUtils
import net.waterfox.android.utils.Settings
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.WeakReference

@RunWith(WaterfoxRobolectricTestRunner::class)
class HomeMenuBuilderTest {

    private lateinit var view: View
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var homeActivity: HomeActivity
    private lateinit var navController: NavController
    private lateinit var menuButton: WeakReference<MenuButton>
    private lateinit var homeMenuBuilder: HomeMenuBuilder

    @Before
    fun setup() {
        view = mockk(relaxed = true)
        lifecycleOwner = mockk(relaxed = true)
        homeActivity = mockk(relaxed = true)
        menuButton = mockk(relaxed = true)
        navController = mockk(relaxed = true)

        homeMenuBuilder = HomeMenuBuilder(
            view = view,
            context = testContext,
            lifecycleOwner = lifecycleOwner,
            homeActivity = homeActivity,
            navController = navController,
            menuButton = menuButton,
            hideOnboardingIfNeeded = {},
        )
    }

    @Test
    fun `WHEN Settings menu item is tapped THEN navigate to settings fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.Settings)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalSettingsFragment()
            )
        }
    }

    @Test
    fun `WHEN Customize Home menu item is tapped THEN navigate to home settings fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.CustomizeHome)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalHomeSettingsFragment()
            )
        }
    }

    @Test
    fun `GIVEN various sync account state WHEN Sync Account menu item is tapped THEN navigate to the appropriate sync fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.SyncAccount(AccountState.AUTHENTICATED))

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalAccountSettingsFragment()
            )
        }

        homeMenuBuilder.onItemTapped(HomeMenu.Item.SyncAccount(AccountState.NEEDS_REAUTHENTICATION))

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalAccountProblemFragment(
                    entrypoint = WaterfoxFxAEntryPoint.HomeMenu,
                )
            )
        }

        homeMenuBuilder.onItemTapped(HomeMenu.Item.SyncAccount(AccountState.NO_ACCOUNT))

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalTurnOnSync(
                    entrypoint = WaterfoxFxAEntryPoint.HomeMenu,
                )
            )
        }
    }

    @Test
    fun `WHEN Bookmarks menu item is tapped THEN navigate to the bookmarks fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.Bookmarks)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalBookmarkFragment(BookmarkRoot.Mobile.id)
            )
        }
    }

    @Test
    fun `WHEN History menu item is tapped THEN navigate to the history fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.History)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalHistoryFragment()
            )
        }
    }

    @Test
    fun `WHEN Downloads menu item is tapped THEN navigate to the downloads fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.Downloads)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalDownloadsFragment()
            )
        }
    }

    @Test
    fun `WHEN Help menu item is tapped THEN open the browser to the SUMO help page`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.Help)

        verify {
            homeActivity.openToBrowserAndLoad(
                searchTermOrURL = SupportUtils.getSumoURLForTopic(
                    context = testContext,
                    topic = SupportUtils.SumoTopic.HELP
                ),
                newTab = true,
                from = BrowserDirection.FromHome
            )
        }
    }

    @Test
    fun `WHEN Reconnect Sync menu item is tapped THEN navigate to the account problem fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.ReconnectSync)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalAccountProblemFragment(
                    entrypoint = WaterfoxFxAEntryPoint.HomeMenu,
                ),
            )
        }
    }

    @Test
    fun `WHEN Extensions menu item is tapped THEN navigate to the addons management fragment`() {
        homeMenuBuilder.onItemTapped(HomeMenu.Item.Extensions)

        verify {
            navController.nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalAddonsManagementFragment()
            )
        }
    }

    @Test
    fun `WHEN Desktop Mode menu item is tapped THEN set the desktop mode settings`() {
        every { testContext.settings() } returns Settings(testContext)

        homeMenuBuilder.onItemTapped(HomeMenu.Item.DesktopMode(checked = true))

        assertTrue(testContext.settings().openNextTabInDesktopMode)
    }
}
