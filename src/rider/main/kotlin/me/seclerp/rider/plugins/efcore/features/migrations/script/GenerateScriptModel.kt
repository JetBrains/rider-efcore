package me.seclerp.rider.plugins.efcore.features.migrations.script

import me.seclerp.rider.plugins.efcore.ui.items.MigrationItem

data class GenerateScriptModel(
    var fromMigration: MigrationItem?,
    var toMigration: MigrationItem?,
    var outputFilePath: String,
    var idempotent: Boolean,
    var noTransactions: Boolean
)