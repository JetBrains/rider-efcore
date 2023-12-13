package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions

class AddMigrationCommand(
    common: DialogCommonOptions,
    val migrationName: String,
    val outputFolder: String
) : DialogCommand(common)