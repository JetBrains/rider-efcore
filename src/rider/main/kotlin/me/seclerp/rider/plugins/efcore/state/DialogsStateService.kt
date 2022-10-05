package me.seclerp.rider.plugins.efcore.state

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service
@State(name = "EfCoreDialogsState", storages = [Storage("efCoreDialogsState.xml")])
class DialogsStateService : PersistentStateComponent<DialogsState> {
    private var myState = DialogsState()

    override fun getState(): DialogsState = myState

    override fun loadState(state: DialogsState) {
        myState = state
    }

    fun forDialog(dialogId: String) =
        SpecificDialogState(dialogId, myState.keyValueStorage)

    class SpecificDialogState(
        private val dialogId: String,
        private val storage: MutableMap<String, String>
    ) {
        fun get(key: String) =
            storage["${dialogId}:${key}"]

        fun getBool(key: String) =
            get(key)?.toBoolean()

        fun getInt(key: String) =
            get(key)?.toInt()

        fun set(key: String, value: String) {
            storage["${dialogId}:${key}"] = value
        }
    }
}