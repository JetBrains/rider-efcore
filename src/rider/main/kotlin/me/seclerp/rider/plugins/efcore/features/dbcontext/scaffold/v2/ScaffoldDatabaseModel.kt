package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold.v2

import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem
import me.seclerp.rider.plugins.efcore.ui.items.SimpleListTableModel

data class ScaffoldDatabaseModel(
    var connection: String,
    var provider: String,
    var outputFolder: String,

    var useAttributes: Boolean,
    var useDatabaseNames: Boolean,
    var generateOnConfiguring: Boolean,
    var usePluralizer: Boolean,

    var dbContextName: String,
    var dbContextFolder: String,

    val tablesList: MutableList<SimpleItem>,
    val schemasList: MutableList<SimpleItem>,

    var scaffoldAllTables: Boolean,
    var scaffoldAllSchemas: Boolean
)
