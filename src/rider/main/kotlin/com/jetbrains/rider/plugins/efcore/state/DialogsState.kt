package com.jetbrains.rider.plugins.efcore.state

import com.intellij.credentialStore.CredentialAttributes

class DialogsState {
    var keyValueStorage = mutableMapOf<String, Any?>()
    val storedSecureAttributes = mutableSetOf<CredentialAttributes>()
}