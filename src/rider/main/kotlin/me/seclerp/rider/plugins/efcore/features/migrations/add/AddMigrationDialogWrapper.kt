package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.observables.withLogger
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import me.seclerp.rider.plugins.efcore.ui.bindText
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import java.io.File

class AddMigrationDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectName: String?,
) : CommonDialogWrapper<AddMigrationDataContext>(
    AddMigrationDataContext(intellijProject),
    toolsVersion,
    "Add Migration",
    intellijProject,
    selectedProjectName
) {
    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Internal data
    private val migrationProjectFolder = observable("").withLogger("migrationProjectFolder")

    //
    // Validation
    private val validator = AddMigrationValidator(dataCtx)

    //
    // Constructor
    init {
        initUi()
    }

    override fun initBindings() {
        super.initBindings()

        migrationProjectFolder.bind(dataCtx.migrationsProject) {
            if (it != null)
                File(it.fullPath).parentFile.path
            else
                ""
        }
    }

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()
        val migrationName = dataCtx.migrationName.value.trim()
        val migrationsOutputFolder = dataCtx.migrationsOutputFolder.value

        return migrationsCommandFactory.add(commonOptions, migrationName, migrationsOutputFolder)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        row("Migration name:") {
            textField()
                .bindText(dataCtx.migrationName)
                .horizontalAlign(HorizontalAlign.FILL)
                .validationOnInput(validator.migrationNameValidation())
                .validationOnApply(validator.migrationNameValidation())
                .focused()
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange("Additional Options"){
            row("Migrations folder:") {
                textFieldForRelativeFolder(migrationProjectFolder.getter, intellijProject, "Select Migrations Folder")
                    .bindText(dataCtx.migrationsOutputFolder)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput(validator.migrationsOutputFolderValidation())
                    .validationOnApply(validator.migrationsOutputFolderValidation())
                    .applyToComponent {
                        dataCtx.migrationsProject.afterChange { isEnabled = it != null }
                    }
            }
        }
    }
}