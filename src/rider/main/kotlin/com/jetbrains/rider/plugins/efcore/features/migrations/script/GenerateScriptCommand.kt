package com.jetbrains.rider.plugins.efcore.features.migrations.script

import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions

class GenerateScriptCommand(
    common: DialogCommonOptions,
    val fromMigration: String,
    val toMigration: String?,
    val outputFilePath: String,
    val idempotent: Boolean,
    val noTransactions: Boolean,
) : DialogCommand(common)