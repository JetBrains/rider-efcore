package me.seclerp.rider.plugins.efcore.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class EfCoreUiConfigurable : BoundConfigurable("EF Core UI") {
    private val settingsStateService = service<EfCoreUiSettingsStateService>()

    override fun createPanel() = panel {
        group("Dialogs Data") {
            row {
                checkBox("Use previously selected options in dialogs")
                    .bindSelected(settingsStateService::usePreviouslySelectedOptionsInDialogs)
                    .comment("<b>Experimental</b>: If enabled, next opened dialog instance will reuse data from a previous one")
            }
        }
    }
}