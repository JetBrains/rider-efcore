package com.jetbrains.rider.plugins.efcore.state

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.database.util.common.castTo
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
        val dialogId: String,
        private val storageMap: MutableMap<String, Any?>,
        private val storedSecureAttributes: MutableSet<CredentialAttributes>
    ) {
        val storage: Map<String, Any?> get() = storageMap

        @NonNls
        inline fun <reified T> get(key: String) =
            storage["${dialogId}:${key}"] as? T

        @NonNls
        fun <T> set(key: String, value: T) {
            storageMap["${dialogId}:${key}"] = value as Any
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