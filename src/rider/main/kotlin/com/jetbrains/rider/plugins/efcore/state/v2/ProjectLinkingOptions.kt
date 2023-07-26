package com.jetbrains.rider.plugins.efcore.state.v2

import kotlinx.serialization.Serializable

@Serializable
data class ProjectLinkingOptions(
    val migrationsToStartupProjects: MutableMap<String, String> = mutableMapOf(),
    val startupToMigrationsProjects: MutableMap<String, String> = mutableMapOf()
)