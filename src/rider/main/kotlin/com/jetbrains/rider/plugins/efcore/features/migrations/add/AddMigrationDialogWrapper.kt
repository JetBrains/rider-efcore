package com.jetbrains.rider.plugins.efcore.features.migrations.add

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.jetbrains.observables.bind
import com.jetbrains.observables.observable
import com.jetbrains.observables.ui.dsl.bindText
import com.jetbrains.observables.withLogger
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.ui.AnyInputDocumentListener
import com.jetbrains.rider.plugins.efcore.ui.textFieldForRelativeFolder
import org.jetbrains.annotations.NonNls
import java.io.File
import java.util.*

class AddMigrationDialogWrapper(
    toolsVersion: DotnetEfVersion,
    intellijProject: Project,
    selectedProjectId: UUID?,
) : CommonDialogWrapper<AddMigrationDataContext>(
    AddMigrationDataContext(intellijProject),
    toolsVersion,
    EfCoreUiBundle.message("action.EfCore.Features.Migrations.AddMigrationAction.text"),
    intellijProject,
    selectedProjectId
) {
    //
    // Internal data
    private val migrationProjectFolder = observable("").withLogger("migrationProjectFolder")
    private val userInputReceived = observable(false).withLogger("userInputReceived")
    private val migrationNameChangedListener = AnyInputDocumentListener(userInputReceived)

    //
    // Validation
    private val validator = AddMigrationValidator(dataCtx, migrationProjectFolder.getter)

    //
    // Constructor
    init {
        migrationProjectFolder.afterChange { doValidateAll() }
        initUi()
    }

    override fun getHelpId() = "EFCore.Features.Migrations.AddMigration"

    override fun initBindings() {
        super.initBindings()

        migrationProjectFolder.bind(dataCtx.migrationsProject) {
            if (it != null)
                File(it.fullPath).parentFile.path
            else
                ""
        }
    }

    override fun generateCommand(): DialogCommand {
        val commonOptions = getCommonOptions()
        val migrationName = dataCtx.migrationName.value.trim()
        val migrationsOutputFolder = dataCtx.migrationsOutputFolder.value.ifEmpty { "." }

        return AddMigrationCommand(commonOptions, migrationName, migrationsOutputFolder)
    }

    //
    // UI
    override fun Panel.createPrimaryOptions() {
        row(EfCoreUiBundle.message("migration.name")) {
            textField()
                .bindText(dataCtx.migrationName)
                .align(AlignX.FILL)
                .validationOnInput(validator.migrationNameValidation())
                .validationOnApply(validator.migrationNameValidation())
                .focused()
                .applyToComponent {
                    setupInitialMigrationNameListener(this)
                }
        }
    }

    override fun Panel.createAdditionalGroup() {
        groupRowsRange(EfCoreUiBundle.message("section.additional.options")){
            row(EfCoreUiBundle.message("migrations.folder")) {
                textFieldForRelativeFolder(migrationProjectFolder.getter, intellijProject,
                    EfCoreUiBundle.message("select.migrations.folder"))
                    .bindText(dataCtx.migrationsOutputFolder)
                    .align(AlignX.FILL)
                    .applyToComponent { dataCtx.migrationsProject.afterChange { isEnabled = it != null } }
                    .validationOnInput(validator.migrationsOutputFolderValidation())
                    .validationOnApply(validator.migrationsOutputFolderValidation())
                    .comment(EfCoreUiBundle.message("text.field.for.relative.folder.comment"))
            }
        }
    }

    private fun setupInitialMigrationNameListener(migrationNameField: JBTextField) {
        dataCtx.availableMigrations.afterChange {
            if (!userInputReceived.value) {
                @NonNls
                val migrationName = if (it.isNotEmpty()) "" else "Initial"

                if (migrationNameField.text != migrationName) {
                    migrationNameField.document.removeDocumentListener(migrationNameChangedListener)
                    migrationNameField.text = migrationName
                    migrationNameField.document.addDocumentListener(migrationNameChangedListener)
                }
            }
        }
    }
}