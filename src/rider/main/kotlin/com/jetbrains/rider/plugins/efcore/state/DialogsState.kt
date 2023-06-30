package com.jetbrains.rider.plugins.efcore.state

import com.intellij.credentialStore.CredentialAttributes

class DialogsState {
    var keyValueStorage = mutableMapOf<String, String>()
    val storedSecureAttributes = mutableSetOf<CredentialAttributes>()
}