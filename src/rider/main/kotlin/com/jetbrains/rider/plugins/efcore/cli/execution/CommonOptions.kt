package com.jetbrains.rider.plugins.efcore.cli.execution

data class CommonOptions(
    val migrationsProject: String,
    val startupProject: String,
    val dbContext: String?,
    val buildConfiguration: String,
    val targetFramework: String?,
    val noBuild: Boolean = false,
    val enableDiagnosticLogging: Boolean = false,
    val additionalArguments: String = ""
)