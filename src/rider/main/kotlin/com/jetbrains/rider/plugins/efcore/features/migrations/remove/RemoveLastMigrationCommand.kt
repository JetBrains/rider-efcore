package com.jetbrains.rider.plugins.efcore.features.migrations.remove

import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions

class RemoveLastMigrationCommand(
    common: DialogCommonOptions,
) : DialogCommand(common)