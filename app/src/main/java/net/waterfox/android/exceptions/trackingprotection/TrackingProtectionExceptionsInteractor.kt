/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android.exceptions.trackingprotection

import mozilla.components.concept.engine.content.blocking.TrackingProtectionException
import mozilla.components.feature.session.TrackingProtectionUseCases
import net.waterfox.android.BrowserDirection
import net.waterfox.android.HomeActivity
import net.waterfox.android.exceptions.ExceptionsInteractor
import net.waterfox.android.settings.SupportUtils

interface TrackingProtectionExceptionsInteractor : ExceptionsInteractor<TrackingProtectionException> {
    /**
     * Called whenever learn more about tracking protection is tapped
     */
    fun onLearnMore()
}

class DefaultTrackingProtectionExceptionsInteractor(
    private val activity: HomeActivity,
    private val exceptionsStore: ExceptionsFragmentStore,
    private val trackingProtectionUseCases: TrackingProtectionUseCases
) : TrackingProtectionExceptionsInteractor {

    override fun onLearnMore() {
        activity.openToBrowserAndLoad(
            searchTermOrURL = SupportUtils.getGenericSumoURLForTopic(
                SupportUtils.SumoTopic.TRACKING_PROTECTION
            ),
            newTab = true,
            from = BrowserDirection.FromTrackingProtectionExceptions
        )
    }

    override fun onDeleteAll() {
        trackingProtectionUseCases.removeAllExceptions()
        reloadExceptions()
    }

    override fun onDeleteOne(item: TrackingProtectionException) {
        trackingProtectionUseCases.removeException(item)
        reloadExceptions()
    }

    fun reloadExceptions() {
        trackingProtectionUseCases.fetchExceptions { resultList ->
            exceptionsStore.dispatch(
                ExceptionsFragmentAction.Change(resultList)
            )
        }
    }
}
