/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.waterfox.android.home.sessioncontrol.viewholders.onboarding

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.waterfox.android.R
import net.waterfox.android.components.toolbar.ToolbarPosition
import net.waterfox.android.databinding.OnboardingToolbarPositionPickerBinding
import net.waterfox.android.ext.asActivity
import net.waterfox.android.ext.components
import net.waterfox.android.onboarding.OnboardingRadioButton
import net.waterfox.android.utils.view.addToRadioGroup

class OnboardingToolbarPositionPickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    init {
        val binding = OnboardingToolbarPositionPickerBinding.bind(view)

        val radioTopToolbar = binding.toolbarTopRadioButton
        val radioBottomToolbar = binding.toolbarBottomRadioButton
        val radio: OnboardingRadioButton

        addToRadioGroup(radioTopToolbar, radioBottomToolbar)
        radioTopToolbar.addIllustration(binding.toolbarTopImage)
        radioBottomToolbar.addIllustration(binding.toolbarBottomImage)

        val settings = view.context.components.settings
        radio = when (settings.toolbarPosition) {
            ToolbarPosition.BOTTOM -> radioBottomToolbar
            ToolbarPosition.TOP -> radioTopToolbar
        }
        radio.updateRadioValue(true)

        radioBottomToolbar.onClickListener {
            itemView.context.asActivity()?.recreate()
        }

        binding.toolbarBottomImage.setOnClickListener {
            radioBottomToolbar.performClick()
        }

        radioTopToolbar.onClickListener {
            itemView.context.asActivity()?.recreate()
        }

        binding.toolbarTopImage.setOnClickListener {
            radioTopToolbar.performClick()
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_toolbar_position_picker
    }
}
