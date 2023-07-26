package com.jetbrains.rider.plugins.efcore.state.v2

import kotlinx.serialization.Serializable

@Serializable
data class RootOptions(
    val projectLinkingOptions: ProjectLinkingOptions = ProjectLinkingOptions(),
    val commonOptions: CommonOptions = CommonOptions(),
    val addMigrationOptions: AddMigrationOptions = AddMigrationOptions(),
    val removeLastMigrationOptions: RemoveLastMigrationOptions = RemoveLastMigrationOptions(),
    val generateScriptOptions: GenerateScriptOptions = GenerateScriptOptions(),
    val updateDatabaseOptions: UpdateDatabaseOptions = UpdateDatabaseOptions(),
    val dropDatabaseOptions: DropDatabaseOptions = DropDatabaseOptions(),
    val scaffoldDbContextOptions: ScaffoldDbContextOptions = ScaffoldDbContextOptions()
)