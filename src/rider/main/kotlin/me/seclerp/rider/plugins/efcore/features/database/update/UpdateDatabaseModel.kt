package me.seclerp.rider.plugins.efcore.features.database.update

data class UpdateDatabaseModel(
    var targetMigration: String,
    var useDefaultConnection: Boolean,
    var connection: String
)