package me.seclerp.rider.plugins.efcore.features.shared.models

import java.util.*

data class StartupProjectData(
    val id: UUID,
    val fullPath: String,
    val targetFrameworks: List<String>
)
