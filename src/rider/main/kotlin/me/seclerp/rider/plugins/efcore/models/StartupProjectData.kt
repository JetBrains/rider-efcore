package me.seclerp.rider.plugins.efcore.models

import java.util.*

data class StartupProjectData(
    val id: UUID,
    val fullPath: String,
    val targetFrameworks: List<String>
)
