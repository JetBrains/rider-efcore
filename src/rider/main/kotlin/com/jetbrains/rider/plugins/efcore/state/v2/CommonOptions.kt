package com.jetbrains.rider.plugins.efcore.state.v2

import kotlinx.serialization.Serializable

@Serializable
data class CommonOptions(
    val migrationsProject: String? = null
)

