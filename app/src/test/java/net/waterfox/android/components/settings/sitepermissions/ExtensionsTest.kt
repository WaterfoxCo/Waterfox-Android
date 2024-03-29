/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android.components.settings.sitepermissions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.support.ktx.kotlin.getOrigin
import org.junit.Test
import net.waterfox.android.components.Components
import net.waterfox.android.settings.sitepermissions.tryReloadTabBy

class ExtensionsTest {

    @Test
    fun `tryReloadTabBy reloads latest tab matching origin`() {
        val store = BrowserStore(
            BrowserState(
                tabs = listOf(
                    createTab(id = "1", url = "https://www.mozilla.org/1", lastAccess = 1),
                    createTab(id = "2", url = "https://www.mozilla.org/2", lastAccess = 2),
                    createTab(id = "3", url = "https://www.waterfox.net")
                )
            )
        )

        val components: Components = mockk(relaxed = true)
        every { components.core.store } returns store

        components.tryReloadTabBy("https://www.baidu.com/".getOrigin()!!)
        verify(exactly = 0) { components.useCases.sessionUseCases.reload(any()) }

        components.tryReloadTabBy("https://www.mozilla.org".getOrigin()!!)
        verify { components.useCases.sessionUseCases.reload("2") }

        components.tryReloadTabBy("https://www.waterfox.net".getOrigin()!!)
        verify { components.useCases.sessionUseCases.reload("3") }
    }
}
