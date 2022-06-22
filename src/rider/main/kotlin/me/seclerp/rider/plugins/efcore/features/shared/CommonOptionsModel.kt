package me.seclerp.rider.plugins.efcore.features.shared

import me.seclerp.rider.plugins.efcore.ui.items.*

data class CommonOptionsModel(
    var migrationsProject: MigrationsProjectItem? = null,
    var startupProject: StartupProjectItem? = null,
    var dbContext: DbContextItem? = null,
    var buildConfiguration: BuildConfigurationItem? = null,
    var targetFramework: BaseTargetFrameworkItem? = null,
    var noBuild: Boolean = false,
    var additionalArguments: String = ""
)