package com.jetbrains.rider.plugins.efcore.features.migrations.script

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.jetbrains.observables.bind
import com.jetbrains.observables.observable
import com.jetbrains.observables.observableList
import com.jetbrains.observables.ui.dsl.bindSelected
import com.jetbrains.observables.ui.dsl.bindText
import com.jetbrains.observables.ui.dsl.iconComboBox
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.ui.items.MigrationItem
import java.util.*

class GenerateScriptDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?
) : CommonDialogWrapper<GenerateScriptDataContext>(
    GenerateScriptDataContext(intellijProject, toolsVersion),
    toolsVersion,
    EfCoreUiBundle.message("generate.sql.script"),
    intellijProject,
    selectedProjectId
) {

    //
    // Internal data
    private val fromMigrationsView = observableList<MigrationItem?>()
    private val toMigrationsView = observableList<MigrationItem?>()

    private val fromMigrationView = observable<MigrationItem?>(null)
    private val toMigrationView = observable<MigrationItem?>(null)

    //
    // Constructor
    init {
        initUi()
    }

    override fun initBindings() {
        super.initBindings()

        fromMigrationsView.bind(dataCtx.availableFromMigrationNames) {
            it.map(mappings.migration.toItem)
        }

        toMigrationsView.bind(dataCtx.availableToMigrationNames) {
            it.map(mappings.migration.toItem)
        }

        fromMigrationView.bind(fromMigrationsView) { it.lastOrNull() }
        toMigrationView.bind(toMigrationsView) { it.firstOrNull() }

        fromMigrationView.bind(dataCtx.fromMigration,
            mappings.migration.toItem,
            mappings.migration.fromItem)

        toMigrationView.bind(dataCtx.toMigration,
            mappings.migration.toItem,
            mappings.migration.fromItem)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        createMigrationRows()
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange(EfCoreUiBundle.message("section.additional.options")) {
            row(EfCoreUiBundle.message("output.file")) {
                textField()
                    .bindText(dataCtx.outputFilePath)
                    .validationOnApply { dataCtx.outputFileValidation(it.text)?.forComponent(it) }
                    .validationOnInput { dataCtx.outputFileValidation(it.text)?.forComponent(it) }
            }
            row {
                checkBox(EfCoreUiBundle.message("checkbox.make.script.idempotent"))
                    .bindSelected(dataCtx.idempotent)
            }
            if (efCoreVersion.major >= 5) {
                row {
                    checkBox(EfCoreUiBundle.message("checkbox.no.transactions"))
                        .bindSelected(dataCtx.noTransactions)
                }
            }
        }
    }

    private fun Panel.createMigrationRows() {
        row(EfCoreUiBundle.message("from.migration")) {
            iconComboBox(fromMigrationView, fromMigrationsView)
                .validationOnApply { dataCtx.fromMigrationValidation(it.item.data)?.forComponent(it) }
                .validationOnInput { dataCtx.fromMigrationValidation(it.item.data)?.forComponent(it) }
                .comment(EfCoreUiBundle.message("before.first.migration.comment"))
                .align(AlignX.FILL)
                .focused()
        }

        row(EfCoreUiBundle.message("to.migration")) {
            iconComboBox(toMigrationView, toMigrationsView)
                .align(AlignX.FILL)
                .focused()
        }
    }

    companion object {
        private object mappings {
            object migration {
                val toItem: (String?) -> MigrationItem?
                    get() = {
                        if (it == null) null else MigrationItem(it)
                    }

                val fromItem: (MigrationItem?) -> String?
                    get() = { it?.data }
            }
        }
    }
}