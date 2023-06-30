package com.jetbrains.rider.plugins.efcore.state

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.*
import org.jetbrains.annotations.NonNls

@Service
@State(name = "EfCoreDialogsState", storages = [Storage("efCoreDialogsState.xml")])
class DialogsStateService : PersistentStateComponent<DialogsState> {
    companion object {
        fun getInstance(): DialogsStateService = service()
    }

    private var myState = DialogsState()

    override fun getState(): DialogsState = myState

    override fun loadState(state: DialogsState) {
        myState = state
    }

    fun clearState() {
        myState.keyValueStorage.clear()
        myState.storedSecureAttributes.forEach {
            PasswordSafe.instance.set(it, null)
        }
        myState.storedSecureAttributes.clear()
    }

    @NonNls
    fun forDialog(dialogId: String) =
        SpecificDialogState(dialogId, myState.keyValueStorage, myState.storedSecureAttributes)

    @NonNls
    class SpecificDialogState(
        private val dialogId: String,
        private val storage: MutableMap<String, String>,
        private val storedSecureAttributes: MutableSet<CredentialAttributes>
    ) {

        @NonNls
        fun get(key: String) =
            storage["${dialogId}:${key}"]

        @NonNls
        fun getBool(key: String) =
            get(key)?.toBoolean()

        @NonNls
        fun set(key: String, value: String) {
            storage["${dialogId}:${key}"] = value
        }

        @NonNls
        fun set(key: String, value: Boolean) {
            storage["${dialogId}:${key}"] = value.toString()
        }

        @NonNls
        fun getSensitive(key: String): String? {
            val attributes = createCredentialAttributes(key)
            return PasswordSafe.instance.get(attributes)?.password?.toString()
        }

        @NonNls
        fun setSensitive(key: String, value: String) {
            val attributes = createCredentialAttributes(key)
            storedSecureAttributes.add(attributes)
            PasswordSafe.instance.set(attributes, Credentials("EfCoreUiPlugin", value))
        }

        @NonNls
        private fun createCredentialAttributes(key: String) = CredentialAttributes(
            generateServiceName("EfCoreDialogsData", "$dialogId:$key")
        )
    }
}