package com.jetbrains.rider.plugins.efcore.state.v2

import kotlinx.serialization.Serializable

@Serializable
data class ScaffoldDbContextOptions(
    val provider: String? = null,
    val outputFolder: String? = null,
    val useAttributes: Boolean? = null,
    val useDatabaseNames: Boolean? = null,
    val generateOnConfiguring: Boolean? = null,
    val usePluralizer: Boolean? = null,
    val dbContextName: String? = null,
    val dbContextFolder: String? = null,
)