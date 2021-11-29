package me.seclerp.rider.plugins.efcore.models

data class StartupProjectData(
    val fullPath: String,
    val targetFrameworks: List<String>
)
