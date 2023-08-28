package com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.observables.ObservableCollection
import com.jetbrains.observables.observable
import com.jetbrains.observables.observableList
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.DbContextCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableConnections
import com.jetbrains.rider.plugins.efcore.features.shared.ObservableDbProviders
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.rd.DbProviderInfo
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService
import com.jetbrains.rider.plugins.efcore.ui.items.SimpleItem
import org.jetbrains.annotations.NonNls
import javax.swing.JPanel

class ScaffoldDbContextDataContext(intellijProject: Project, private val efCoreVersion: DotnetEfVersion) : CommonDataContext(intellijProject, false, false) {
    private val listSeparator = "@_#@@"

    private val dbContextCommandFactory = intellijProject.service<DbContextCommandFactory>()

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

    val connectionValidation: (String?) -> ValidationInfo? = {
        if (it.isNullOrEmpty())
            error(EfCoreUiBundle.message("dialog.message.connection.could.not.be.empty"))
        else null
    }

    val providerValidation: (DbProviderInfo?) -> ValidationInfo? = {
        if (it == null)
            error(EfCoreUiBundle.message("dialog.message.provider.should.not.be.empty"))
        else
            null
    }

    val outputFolderValidation: (String?) -> ValidationInfo? = {
        if (it?.trim().isNullOrEmpty())
            error(EfCoreUiBundle.message("dialog.message.output.folder.should.not.be.empty"))
        else
            null
    }

    val dbContextNameValidation: (String?) -> ValidationInfo? = {
        if (it?.trim().isNullOrEmpty())
            error(EfCoreUiBundle.message("dialog.message.dbcontext.class.name.could.not.be.empty"))
        else
            null
    }

    val dbContextFolderValidation: (String?) -> ValidationInfo? = {
        if (it?.trim().isNullOrEmpty())
            error(EfCoreUiBundle.message("dialog.message.dbcontext.folder.should.not.be.empty"))
        else
            null
    }

    fun tableSchemaValidation(
        tablesList: ObservableCollection<SimpleItem>,
        scaffoldAllTables: ComponentPredicate
    ): ValidationInfoBuilder.(JPanel) -> ValidationInfo? = {
        if (!scaffoldAllTables.invoke() && tablesList.none { it.data.isNotEmpty() })
            error(EfCoreUiBundle.message("dialog.message.tables.schemas.should.not.be.empty"))
        else
            null
    }

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
            tablesList.clear()
            tablesList.addAll(this.split(listSeparator).map { SimpleItem(it) })
            scaffoldAllTables.value = this.isEmpty()
        }

        commonDialogState.get(KnownStateKeys.SCHEMAS)?.apply {
            schemasList.clear()
            schemasList.addAll(this.split(listSeparator).map { SimpleItem(it) })
            scaffoldAllSchemas.value = this.isEmpty()
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
        commonDialogState.set(KnownStateKeys.TABLES, filteredTables.joinToString(listSeparator) { it.data })

        val filteredSchemas = schemasList.value.filter { it.data.isNotEmpty() }
        commonDialogState.set(KnownStateKeys.SCHEMAS, filteredSchemas.joinToString(listSeparator) { it.data })
    }

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()

        return dbContextCommandFactory.scaffold(
            efCoreVersion, commonOptions,
            connection.value,
            provider.value,
            outputFolder.value,
            useAttributes.value,
            useDatabaseNames.value,
            generateOnConfiguring.value,
            usePluralizer.value,
            dbContextName.value,
            dbContextFolder.value,
            scaffoldAllTables.value,
            tablesList.map { it.data },
            scaffoldAllSchemas.value,
            schemasList.map { it.data })
    }

    override fun validate(): List<ValidationInfo> {
        return super.validate()
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
        @NonNls
        const val SCHEMAS = "schemas"
    }
}