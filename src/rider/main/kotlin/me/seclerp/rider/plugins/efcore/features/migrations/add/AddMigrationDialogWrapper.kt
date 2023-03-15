package me.seclerp.rider.plugins.efcore.features.migrations.add

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.observables.withLogger
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDialogWrapper
import me.seclerp.observables.ui.dsl.bindText
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.ui.AnyInputDocumentListener
import me.seclerp.rider.plugins.efcore.ui.textFieldForRelativeFolder
import org.jetbrains.annotations.NonNls
import java.io.File
import java.util.UUID

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
    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

    //
    // Internal data
    private val migrationProjectFolder = observable("").withLogger("migrationProjectFolder")
    private val userInputReceived = observable(false).withLogger("userInputReceived")
    private val migrationNameChangedListener = AnyInputDocumentListener(userInputReceived)

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

    override fun generateCommand(): GeneralCommandLine {
        val commonOptions = getCommonOptions()
        val migrationName = dataCtx.migrationName.value.trim()
        val migrationsOutputFolder = dataCtx.migrationsOutputFolder.value

        return migrationsCommandFactory.add(commonOptions, migrationName, migrationsOutputFolder)
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
                textFieldForRelativeFolder(migrationProjectFolder.getter, intellijProject, EfCoreUiBundle.message("select.migrations.folder"))
                    .bindText(dataCtx.migrationsOutputFolder)
                    .align(AlignX.FILL)
                    .validationOnInput(validator.migrationsOutputFolderValidation())
                    .validationOnApply(validator.migrationsOutputFolderValidation())
                    .applyToComponent {
                        dataCtx.migrationsProject.afterChange { isEnabled = it != null }
                    }
            }
        }
    }

    @NonNls
    private fun setupInitialMigrationNameListener(migrationNameField: JBTextField) {
        dataCtx.availableMigrations.afterChange {
            if (!userInputReceived.value) {
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