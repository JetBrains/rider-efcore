package com.jetbrains.rider.plugins.efcore.features.shared.dialog

import com.jetbrains.rider.plugins.efcore.rd.DbContextInfo
import com.jetbrains.rider.plugins.efcore.rd.MigrationsProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.StartupProjectInfo

data class DialogCommonOptions(
    val migrationsProject: MigrationsProjectInfo,
    val startupProject: StartupProjectInfo,
    val dbContext: DbContextInfo?,
    val targetFramework: String?,
    val buildConfiguration: String,
    val noBuild: Boolean,
    val enableDiagnosticLogging: Boolean,
    val additionalArguments: String,
)

open class DialogCommand(
    val common: DialogCommonOptions
)

