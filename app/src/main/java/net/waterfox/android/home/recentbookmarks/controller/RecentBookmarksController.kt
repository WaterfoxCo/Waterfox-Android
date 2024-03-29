/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android.home.recentbookmarks.controller

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.navigation.NavController
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineSession.LoadUrlFlags.Companion.ALLOW_JAVASCRIPT_URL
import net.waterfox.android.BrowserDirection
import net.waterfox.android.HomeActivity
import net.waterfox.android.R
import net.waterfox.android.components.AppStore
import net.waterfox.android.components.appstate.AppAction
import net.waterfox.android.home.HomeFragmentDirections
import net.waterfox.android.home.recentbookmarks.RecentBookmark
import net.waterfox.android.home.recentbookmarks.interactor.RecentBookmarksInteractor

/**
 * An interface that handles the view manipulation of the recently saved bookmarks on the
 * Home screen.
 */
interface RecentBookmarksController {

    /**
     * @see [RecentBookmarksInteractor.onRecentBookmarkClicked]
     */
    fun handleBookmarkClicked(bookmark: RecentBookmark)

    /**
     * @see [RecentBookmarksInteractor.onShowAllBookmarksClicked]
     */
    fun handleShowAllBookmarksClicked()

    /**
     * @see [RecentBookmarksInteractor.onRecentBookmarkRemoved]
     */
    fun handleBookmarkRemoved(bookmark: RecentBookmark)
}

/**
 * The default implementation of [RecentBookmarksController].
 */
class DefaultRecentBookmarksController(
    private val activity: HomeActivity,
    private val navController: NavController,
    private val appStore: AppStore,
) : RecentBookmarksController {

    override fun handleBookmarkClicked(bookmark: RecentBookmark) {
        dismissSearchDialogIfDisplayed()
        activity.openToBrowserAndLoad(
            searchTermOrURL = bookmark.url!!,
            newTab = true,
            from = BrowserDirection.FromHome,
            flags = EngineSession.LoadUrlFlags.select(ALLOW_JAVASCRIPT_URL)
        )
    }

    override fun handleShowAllBookmarksClicked() {
        dismissSearchDialogIfDisplayed()
        navController.navigate(
            HomeFragmentDirections.actionGlobalBookmarkFragment(BookmarkRoot.Mobile.id)
        )
    }

    override fun handleBookmarkRemoved(bookmark: RecentBookmark) {
        appStore.dispatch(AppAction.RemoveRecentBookmark(bookmark))
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun dismissSearchDialogIfDisplayed() {
        if (navController.currentDestination?.id == R.id.searchDialogFragment) {
            navController.navigateUp()
        }
    }
}
