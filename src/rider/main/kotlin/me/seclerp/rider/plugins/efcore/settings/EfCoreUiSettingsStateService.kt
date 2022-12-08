package me.seclerp.rider.plugins.efcore.settings

import com.intellij.openapi.components.*

@Service
@State(name = "EfCoreUiSettings", storages = [Storage("efCoreUiSettings.xml")])
class EfCoreUiSettingsStateService : PersistentStateComponent<EfCoreUiSettingsState> {
    companion object {
        fun getInstance(): EfCoreUiSettingsStateService = service()
    }

    private var myState = EfCoreUiSettingsState()

    var usePreviouslySelectedOptionsInDialogs : Boolean
        get() = myState.usePreviouslySelectedOptionsInDialogs
        set(value) { myState.usePreviouslySelectedOptionsInDialogs = value }

    var storeSensitiveData : Boolean
        get() = myState.storeSensitiveData
        set(value) { myState.storeSensitiveData = value }

    var useTerminalExecution : Boolean
        get() = myState.useTerminalExecution
        set(value) { myState.useTerminalExecution = value }

    override fun getState(): EfCoreUiSettingsState = myState

    override fun loadState(state: EfCoreUiSettingsState) {
        myState = state
    }
}