package me.seclerp.rider.plugins.efcore.features.migrations.add

data class AddMigrationModel(
    var migrationName: String,
    var migrationOutputFolder: String
)
