/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android.home.recentsyncedtabs.controller

import androidx.navigation.NavController
import mozilla.components.feature.tabs.TabsUseCases
import net.waterfox.android.R
import net.waterfox.android.components.AppStore
import net.waterfox.android.components.appstate.AppAction
import net.waterfox.android.home.HomeFragmentDirections
import net.waterfox.android.home.recentsyncedtabs.RecentSyncedTab
import net.waterfox.android.home.recentsyncedtabs.interactor.RecentSyncedTabInteractor
import net.waterfox.android.tabstray.Page
import net.waterfox.android.tabstray.TabsTrayAccessPoint

/**
 * An interface that handles the view manipulation of the recent synced tabs in the Home screen.
 */
interface RecentSyncedTabController {
    /**
     * @see [RecentSyncedTabInteractor.onRecentSyncedTabClicked]
     */
    fun handleRecentSyncedTabClick(tab: RecentSyncedTab)

    /**
     * @see [RecentSyncedTabInteractor.onRecentSyncedTabClicked]
     */
    fun handleSyncedTabShowAllClicked()

    /**
     * Handle removing the synced tab from the homescreen.
     *
     * @param tab The recent synced tab to be removed.
     */
    fun handleRecentSyncedTabRemoved(tab: RecentSyncedTab)
}

/**
 * The default implementation of [RecentSyncedTabController].
 *
 * @property tabsUseCase Use cases to open the synced tab when clicked.
 * @property navController [NavController] to navigate to synced tabs tray.
 */
class DefaultRecentSyncedTabController(
    private val tabsUseCase: TabsUseCases,
    private val navController: NavController,
    private val accessPoint: TabsTrayAccessPoint,
    private val appStore: AppStore,
) : RecentSyncedTabController {
    override fun handleRecentSyncedTabClick(tab: RecentSyncedTab) {
        tabsUseCase.selectOrAddTab(tab.url)
        navController.navigate(R.id.browserFragment)
    }

    override fun handleSyncedTabShowAllClicked() {
        navController.navigate(
            HomeFragmentDirections.actionGlobalTabsTrayFragment(
                page = Page.SyncedTabs,
                accessPoint = accessPoint
            )
        )
    }

    override fun handleRecentSyncedTabRemoved(tab: RecentSyncedTab) {
        appStore.dispatch(AppAction.RemoveRecentSyncedTab(tab))
    }
}
