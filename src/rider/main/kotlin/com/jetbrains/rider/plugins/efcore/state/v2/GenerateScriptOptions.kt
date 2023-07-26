package com.jetbrains.rider.plugins.efcore.state.v2

import kotlinx.serialization.Serializable

@Serializable
data class GenerateScriptOptions(
    val outputFile: String? = null,
    val idempotent: Boolean? = null,
    val noTransactions: Boolean? = null
)