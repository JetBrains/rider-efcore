package com.jetbrains.rider.plugins.efcore.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService

class EfCoreUiConfigurable : BoundConfigurable(EfCoreUiBundle.message("configurable.name.ef.core.ui")) {
    private val settingsStateService = EfCoreUiSettingsStateService.getInstance()

    override fun createPanel() = panel {
        group(EfCoreUiBundle.message("section.autofill")) {
            row {
                checkBox(EfCoreUiBundle.message("checkbox.autofill.general.options"))
                    .bindSelected(settingsStateService::usePreviouslySelectedOptionsInDialogs)
                    .align(AlignX.FILL)
                    .comment(EfCoreUiBundle.message("checkbox.autofill.general.options.comment"))
            }
            row {
                checkBox(EfCoreUiBundle.message("checkbox.autofill.additional.options"))
                    .bindSelected(settingsStateService::storeSensitiveData)
                    .align(AlignX.FILL)
                    .comment(EfCoreUiBundle.message("checkbox.autofill.additional.options.comment"))
            }
            row {
                button(EfCoreUiBundle.message("button.clear.stored.options")) {
                    DialogsStateService.getInstance().clearState()
                }
            }
        }
        group(EfCoreUiBundle.message("section.execution")) {
            row {
                checkBox(EfCoreUiBundle.message("checkbox.execute.commands.in.terminal"))
                    .bindSelected(settingsStateService::useTerminalExecution)
                    .align(AlignX.FILL)
                    .comment(EfCoreUiBundle.message("execute.in.terminal.comment"))
            }
        }
    }
}