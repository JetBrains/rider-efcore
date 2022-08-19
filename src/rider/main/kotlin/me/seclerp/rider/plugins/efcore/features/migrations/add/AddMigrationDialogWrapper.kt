package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import me.seclerp.observables.mapNullable
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.dialog.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.bindText
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import java.io.File

class AddMigrationDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedDotnetProjectName: String?,
) : BaseDialogWrapper(toolsVersion, "Add Migration", intellijProject, selectedDotnetProjectName, false) {
    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Data binding
    val dataCtx = AddMigrationDataContext(intellijProject, commonCtx, beModel)

    //
    // Internal data
    private val migrationProjectFolder = commonCtx.migrationsProject.mapNullable {
        if (it == null) {
            ""
        } else {
            val currentMigrationsProject = it.fullPath
            File(currentMigrationsProject).parentFile.path
        }
    }

    //
    // Validation
    private val validator = AddMigrationValidator()

    //
    // Constructor
    init {
        init()
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()
        val migrationName = dataCtx.migrationName.notNullValue.trim()
        val migrationsOutputFolder = dataCtx.migrationsOutputFolder.notNullValue

        return migrationsCommandFactory.add(commonOptions, migrationName, migrationsOutputFolder)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        createMigrationNameRow()
    }

    private fun Panel.createMigrationNameRow() {
        row("Migration name:") {
            textField()
                .bindText(dataCtx.migrationName)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput { validator.migrationNameValidation(dataCtx.availableMigrations.notNullValue)(it) }
                .validationOnApply { validator.migrationNameValidation(dataCtx.availableMigrations.notNullValue)(it) }
                .focused()
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options"){
            row("Migrations folder:") {
                textFieldForRelativeFolder({ migrationProjectFolder.notNullValue }, intellijProject, "Select Migrations Folder")
                    .bindText(dataCtx.migrationsOutputFolder)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput(validator.migrationsOutputFolderValidation())
                    .validationOnApply(validator.migrationsOutputFolderValidation())
                    .applyToComponent {
                        commonCtx.migrationsProject.afterChange(warmUp = true) { isEnabled = it != null }
                    }
            }
        }
    }
}