package me.seclerp.rider.plugins.efcore.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.state.DialogsStateService

class EfCoreUiConfigurable : BoundConfigurable(EfCoreUiBundle.message("configurable.name.ef.core.ui")) {
    private val settingsStateService = EfCoreUiSettingsStateService.getInstance()

    override fun createPanel() = panel {
        group(EfCoreUiBundle.message("section.dialogs.data")) {
            row {
                checkBox(EfCoreUiBundle.message("checkbox.use.previously.selected.options.in.dialogs"))
                    .bindSelected(settingsStateService::usePreviouslySelectedOptionsInDialogs)
                    .align(AlignX.FILL)
                    .comment(EfCoreUiBundle.message("reuse.data.comment"))
            }
            row {
                checkBox(EfCoreUiBundle.message("checkbox.store.sensitive.data.in.secure.store"))
                    .bindSelected(settingsStateService::storeSensitiveData)
                    .align(AlignX.FILL)
                    .comment(EfCoreUiBundle.message("sensitive.reuse.comment"))
            }
            row {
                button(EfCoreUiBundle.message("button.clear.stored.data")) {
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