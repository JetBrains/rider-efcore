package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.openapi.util.NlsContexts

data class CliCommandPresentationInfo(
    @NlsContexts.TabTitle
    val name: String,
    @NlsContexts.NotificationContent
    val onSuccessNotification: String
)