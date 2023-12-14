package com.jetbrains.rider.plugins.efcore.features.shared.dialog

import com.jetbrains.rider.plugins.efcore.rd.DbContextInfo
import com.jetbrains.rider.plugins.efcore.rd.MigrationsProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.StartupProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.TargetFrameworkId

data class DialogCommonOptions(
    val migrationsProject: MigrationsProjectInfo,
    val startupProject: StartupProjectInfo,
    val dbContext: DbContextInfo?,
    val targetFramework: TargetFrameworkId?,
    val buildConfiguration: String,
    val noBuild: Boolean,
    val enableDiagnosticLogging: Boolean,
    val additionalArguments: String,
)

open class DialogCommand(
    val common: DialogCommonOptions
)

