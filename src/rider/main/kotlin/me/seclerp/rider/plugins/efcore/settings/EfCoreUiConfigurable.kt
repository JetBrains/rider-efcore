package me.seclerp.rider.plugins.efcore.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import me.seclerp.rider.plugins.efcore.state.DialogsStateService

class EfCoreUiConfigurable : BoundConfigurable("EF Core UI") {
    private val settingsStateService = EfCoreUiSettingsStateService.getInstance()

    override fun createPanel() = panel {
        group("Dialogs Data") {
            row {
                checkBox("Use previously selected options in dialogs")
                    .bindSelected(settingsStateService::usePreviouslySelectedOptionsInDialogs)
                    .align(AlignX.FILL)
                    .comment("<b>Experimental</b>: If enabled, next opened dialog instance will reuse data from a previous one")
            }
            row {
                checkBox("Store sensitive data in a secure store")
                    .bindSelected(settingsStateService::storeSensitiveData)
                    .align(AlignX.FILL)
                    .comment("If enabled, plugin will securely store data that may contain credentials, such as \"Additional arguments\" or \"Connection\" fields<br/>" +
                             "<b>Please use this option carefully</b>, as it could lead to accidents on non-local environments")
            }
            row {
                button("Clear Stored Data") {
                    DialogsStateService.getInstance().clearState()
                }
            }
        }
        group("Execution") {
            row {
                checkBox("Execute commands in terminal")
                    .bindSelected(settingsStateService::useTerminalExecution)
                    .align(AlignX.FILL)
                    .comment("If enabled, command will be executed in the terminal with full output available")
            }
        }
    }
}