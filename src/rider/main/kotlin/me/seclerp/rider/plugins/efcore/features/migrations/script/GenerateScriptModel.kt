package me.seclerp.rider.plugins.efcore.features.migrations.script

data class GenerateScriptModel(
    var fromMigration: String,
    var toMigration: String?,
    var outputFilePath: String,
    var idempotent: Boolean,
    var noTransactions: Boolean
)