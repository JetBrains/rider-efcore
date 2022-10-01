package me.seclerp.rider.plugins.efcore.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service
@State(name = "EfCoreUiSettings", storages = [Storage("efCoreUiSettings.xml")])
class EfCoreUiSettingsStateService : PersistentStateComponent<EfCoreUiSettingsState> {
    private var myState = EfCoreUiSettingsState()

    var usePreviouslySelectedOptionsInDialogs : Boolean
        get() = myState.usePreviouslySelectedOptionsInDialogs
        set(value) { myState.usePreviouslySelectedOptionsInDialogs = value }

    var useTerminalExecution : Boolean
        get() = myState.useTerminalExecution
        set(value) { myState.useTerminalExecution = value }

    override fun getState(): EfCoreUiSettingsState = myState

    override fun loadState(state: EfCoreUiSettingsState) {
        myState = state
    }
}