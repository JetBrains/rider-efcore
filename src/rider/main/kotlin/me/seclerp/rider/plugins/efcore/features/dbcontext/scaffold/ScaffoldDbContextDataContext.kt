package me.seclerp.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.openapi.project.Project
import me.seclerp.observables.observable
import me.seclerp.observables.observableList
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.state.DialogsStateService
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
    }

    object KnownStateKeys {
        const val CONNECTION = "connection"
        const val PROVIDER = "provider"
        const val OUTPUT_FOLDER = "outputFolder"
        const val USE_ATTRIBUTES = "useAttributes"
        const val USE_DATABASE_NAMES = "useDatabaseNames"
        const val GENERATE_ON_CONFIGURING = "generateOnConfiguring"
        const val USE_PLURALIZER = "usePluralizer"
        const val DB_CONTEXT_NAME = "dbContextName"
        const val DB_CONTEXT_FOLDER = "dbContextFolder"
    }
}