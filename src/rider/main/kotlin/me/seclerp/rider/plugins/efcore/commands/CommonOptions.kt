package me.seclerp.rider.plugins.efcore.commands

data class CommonOptions(
    val migrationsProject: String,
    val startupProject: String,
    val dbContext: String,
    val buildConfiguration: String,
    val targetFramework: String,
    val noBuild: Boolean = false
)