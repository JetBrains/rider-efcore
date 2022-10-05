package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.project.Project
import me.seclerp.observables.ObservableCollection
import me.seclerp.observables.observable
import me.seclerp.observables.observableList
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.ui.items.SimpleItem

class ScaffoldDbContextDataContext(intellijProject: Project) : CommonDataContext(intellijProject, false) {
    val connection = observable("")
    val provider = observable("")
    val outputFolder = observable("Entities")

    val useAttributes = observable(false)
    val useDatabaseNames = observable(false)
    val generateOnConfiguring = observable(true)
    val usePluralizer = observable(true)

    val dbContextName = observable("MyDbContext")
    val dbContextFolder = observable("Context")

    val tablesList = observableList<SimpleItem>()
    val schemasList = observableList<SimpleItem>()

    val scaffoldAllTables = observable(true)
    val scaffoldAllSchemas = observable(true)
}