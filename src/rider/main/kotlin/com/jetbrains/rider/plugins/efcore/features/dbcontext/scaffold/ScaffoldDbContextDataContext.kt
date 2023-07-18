package com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.project.Project
import com.jetbrains.observables.observable
import com.jetbrains.observables.observableList
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableConnections
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableDbProviders
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService
import com.jetbrains.rider.plugins.efcore.ui.items.SimpleItem
import org.jetbrains.annotations.NonNls

class ScaffoldDbContextDataContext(intellijProject: Project) : CommonDataContext(intellijProject, false) {
    val connection = observable("")
    val observableConnections = ObservableConnections(intellijProject, startupProject)
    val provider = observable("")
    val observableDbProviders = ObservableDbProviders(intellijProject, migrationsProject)
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

    override fun initBindings() {
        super.initBindings()

        observableConnections.initBinding()
        observableDbProviders.initBinding()
    }

    override fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.loadState(commonDialogState)

        if (pluginSettings.storeSensitiveData) {
            commonDialogState.getSensitive(KnownStateKeys.CONNECTION)?.apply {
                connection.value = this
            }
        }

        commonDialogState.get(KnownStateKeys.PROVIDER)?.apply {
            provider.value = this
        }

        commonDialogState.get(KnownStateKeys.OUTPUT_FOLDER)?.apply {
            outputFolder.value = this
        }

        commonDialogState.getBool(KnownStateKeys.USE_ATTRIBUTES)?.apply {
            useAttributes.value = this
        }

        commonDialogState.getBool(KnownStateKeys.USE_DATABASE_NAMES)?.apply {
            useDatabaseNames.value = this
        }

        commonDialogState.getBool(KnownStateKeys.GENERATE_ON_CONFIGURING)?.apply {
            generateOnConfiguring.value = this
        }

        commonDialogState.getBool(KnownStateKeys.USE_PLURALIZER)?.apply {
            usePluralizer.value = this
        }

        commonDialogState.get(KnownStateKeys.DB_CONTEXT_NAME)?.apply {
            dbContextName.value = this
        }

        commonDialogState.get(KnownStateKeys.DB_CONTEXT_FOLDER)?.apply {
            dbContextFolder.value = this
        }

        commonDialogState.get(KnownStateKeys.TABLES)?.apply {
            // We assign the value directly as a listOf() because using tablesList.removeAll() causes the menu to not open at all
            tablesList.value = mutableListOf()
            tablesList.addAll(this.split(',').map { SimpleItem(it) })
            scaffoldAllTables.value = this.isEmpty()
        }
    }

    override fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        super.saveState(commonDialogState)

        if (pluginSettings.storeSensitiveData) {
            commonDialogState.setSensitive(KnownStateKeys.CONNECTION, connection.value)
        }

        commonDialogState.set(KnownStateKeys.PROVIDER, provider.value)
        commonDialogState.set(KnownStateKeys.OUTPUT_FOLDER, outputFolder.value)
        commonDialogState.set(KnownStateKeys.USE_ATTRIBUTES, useAttributes.value)
        commonDialogState.set(KnownStateKeys.USE_DATABASE_NAMES, useDatabaseNames.value)
        commonDialogState.set(KnownStateKeys.GENERATE_ON_CONFIGURING, generateOnConfiguring.value)
        commonDialogState.set(KnownStateKeys.USE_PLURALIZER, usePluralizer.value)
        commonDialogState.set(KnownStateKeys.DB_CONTEXT_NAME, dbContextName.value)
        commonDialogState.set(KnownStateKeys.DB_CONTEXT_FOLDER, dbContextFolder.value)

         val filteredTables = tablesList.value.filter { it.data.isNotEmpty() }
         commonDialogState.set(KnownStateKeys.TABLES, filteredTables.joinToString { it.data })
    }

    object KnownStateKeys {
        @NonNls
        const val CONNECTION = "connection"
        @NonNls
        const val PROVIDER = "provider"
        @NonNls
        const val OUTPUT_FOLDER = "outputFolder"
        @NonNls
        const val USE_ATTRIBUTES = "useAttributes"
        @NonNls
        const val USE_DATABASE_NAMES = "useDatabaseNames"
        @NonNls
        const val GENERATE_ON_CONFIGURING = "generateOnConfiguring"
        @NonNls
        const val USE_PLURALIZER = "usePluralizer"
        @NonNls
        const val DB_CONTEXT_NAME = "dbContextName"
        @NonNls
        const val DB_CONTEXT_FOLDER = "dbContextFolder"
        @NonNls
        const val TABLES = "tables"
    }
}