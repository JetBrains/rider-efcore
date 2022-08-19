package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import me.seclerp.observables.ObservableCollection
import me.seclerp.observables.ObservableProperty
import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem

class ScaffoldDbContextDataContext {
    var connection = ObservableProperty("")
    var provider = ObservableProperty("")
    var outputFolder = ObservableProperty("Entities")

    var useAttributes = ObservableProperty(false)
    var useDatabaseNames = ObservableProperty(false)
    var generateOnConfiguring = ObservableProperty(true)
    var usePluralizer = ObservableProperty(true)

    var dbContextName = ObservableProperty("MyDbContext")
    var dbContextFolder = ObservableProperty("Context")

    val tablesList = ObservableCollection<SimpleItem>()
    val schemasList = ObservableCollection<SimpleItem>()

    var scaffoldAllTables = ObservableProperty(true)
    var scaffoldAllSchemas = ObservableProperty(true)
}