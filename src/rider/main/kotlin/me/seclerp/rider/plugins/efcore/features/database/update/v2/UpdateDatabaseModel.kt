package me.seclerp.rider.plugins.efcore.features.database.update.v2

data class UpdateDatabaseModel(
    var targetMigration: String,
    var useDefaultConnection: Boolean,
    var connection: String
)