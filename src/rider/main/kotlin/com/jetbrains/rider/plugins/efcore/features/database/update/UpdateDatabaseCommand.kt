package com.jetbrains.rider.plugins.efcore.features.database.update

import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions

class UpdateDatabaseCommand(
    common: DialogCommonOptions,
    val targetMigration: String,
    val connection: String?
) : DialogCommand(common)