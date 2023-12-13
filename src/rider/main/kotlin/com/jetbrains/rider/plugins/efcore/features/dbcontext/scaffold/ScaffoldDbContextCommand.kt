package com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold

import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommonOptions

class ScaffoldDbContextCommand(
    common: DialogCommonOptions,
    val connection: String,
    val provider: String,
    val outputFolder: String,
    val useAttributes: Boolean,
    val useDatabaseNames: Boolean,
    val generateOnConfiguring: Boolean,
    val usePluralizer: Boolean,
    val dbContextName: String,
    val dbContextFolder: String,
    val tablesList: List<String>,
    val schemasList: List<String>,
    val scaffoldAllTables: Boolean,
    val scaffoldAllSchemas: Boolean
) : DialogCommand(common)