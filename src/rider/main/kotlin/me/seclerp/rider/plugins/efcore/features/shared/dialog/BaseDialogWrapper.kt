package me.seclerp.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.jetbrains.rd.util.reactive.hasValue
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.features.preview.CommandPreviewDialogWrapper
import me.seclerp.rider.plugins.efcore.features.shared.models.MigrationsProjectData
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bindNullable
import me.seclerp.rider.plugins.efcore.features.shared.models.StartupProjectData
import me.seclerp.rider.plugins.efcore.features.shared.services.PreferredProjectsManager
import me.seclerp.rider.plugins.efcore.rd.*
import me.seclerp.rider.plugins.efcore.settings.EfCoreUiSettingsStateService
import me.seclerp.rider.plugins.efcore.state.DialogsStateService
import me.seclerp.rider.plugins.efcore.ui.*
import me.seclerp.rider.plugins.efcore.ui.items.*
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseDialogWrapper(
    protected val efCoreVersion: DotnetEfVersion,
    dialogTitle: String,
    protected val intellijProject: Project,
    private val selectedDotnetProjectName: String?,
    requireMigrationsInProject: Boolean = false,
    private val requireDbContext: Boolean = true
) : DialogWrapper(true) {

    private val dialogId = dialogTitle.replace(" ", "")

    protected val beModel = intellijProject.solution.riderEfCoreModel

    //
    // Data binding
    val commonCtx = CommonDataContext(intellijProject, beModel, requireDbContext)

    //
    // Internal data
    private val migrationsProjectsModel = DefaultComboBoxModel(
        commonCtx.availableMigrationsProjects.map {
            MigrationsProjectItem(it.name, MigrationsProjectData(it.id, it.fullPath))
        }.toTypedArray()
    )

    private val startupProjectsModel = DefaultComboBoxModel(
        commonCtx.availableStartupProjects.map {
            StartupProjectItem(it.name, StartupProjectData(it.id, it.fullPath, it.targetFrameworks))
        }.toTypedArray()
    )

    private var targetFrameworkModel = DefaultComboBoxModel<BaseTargetFrameworkItem>()
    private var dbContextModel = DefaultComboBoxModel<DbContextItem>()
    private var buildConfigurationModel = DefaultComboBoxModel(
        commonCtx.availableBuildConfigurations.map { BuildConfigurationItem(it) }.toTypedArray())

    private val selectedMigrationsProject = ObservableProperty<MigrationsProjectItem>(null)
    private val selectedStartupProject = ObservableProperty<StartupProjectItem>(null)
    private val selectedTargetFramework = ObservableProperty<BaseTargetFrameworkItem>(null)
    private val selectedDbContext = ObservableProperty<DbContextItem>(null)
    private val selectedBuildConfiguration = ObservableProperty<BuildConfigurationItem>(null)

    private val isSolutionLevelMode = selectedDotnetProjectName == null

    //
    // Validation
    private val validator = BaseDialogValidator(commonCtx, beModel, intellijProject, requireMigrationsInProject)

    //
    // Preferences
    private val preferredProjectsManager = intellijProject.service<PreferredProjectsManager>()
    private val settingsStateService = service<EfCoreUiSettingsStateService>()
    private val dialogsStateService = intellijProject.service<DialogsStateService>()

    //
    // UI
    protected lateinit var panel: DialogPanel

    //
    // Constructor
    init {
        title = dialogTitle

        initPreferredProjects()
    }

    protected fun initUi() {
        init()
        initBindings()
        initData()
    }

    protected open fun initBindings() {
        selectedMigrationsProject.bindNullable(commonCtx.migrationsProject,
            { migrationsProjectsModel.firstOrNull { item -> item.data.id == it?.id } },
            { commonCtx.availableMigrationsProjects.firstOrNull { info -> info.id == it?.data?.id } })

        selectedStartupProject.bindNullable(commonCtx.startupProject,
            { startupProjectsModel.firstOrNull { item -> item.data.id == it?.id } },
            { commonCtx.availableStartupProjects.firstOrNull { info -> info.id == it?.data?.id } })

        selectedDbContext.bindNullable(commonCtx.dbContext,
            { dbContextModel.firstOrNull { item -> item.data == it?.fullName } },
            { commonCtx.availableDbContexts.value?.firstOrNull { info -> info.fullName == it?.data } })

        selectedTargetFramework.bindNullable(commonCtx.targetFramework,
            { targetFrameworkModel.firstOrNull { item -> item.data == it } },
            { commonCtx.availableTargetFrameworks.value?.firstOrNull { tgFm -> tgFm == it?.data } })

        selectedBuildConfiguration.bindNullable(commonCtx.buildConfiguration,
            { buildConfigurationModel.firstOrNull { item -> item.displayName == it } },
            { commonCtx.availableBuildConfigurations.firstOrNull { buildConfig -> buildConfig == it?.displayName } })

        commonCtx.availableTargetFrameworks.afterChange {
            targetFrameworkModel.removeAllElements()
            if (it != null) {
                val targetFrameworks = it.map { it.toTargetFrameworkViewItem() }
                targetFrameworkModel.addAll(targetFrameworks)
            }
        }

        commonCtx.availableDbContexts.afterChange {
            dbContextModel.removeAllElements()
            if (it != null) {
                val dbContextItems = it.map { it.toViewItem() }
                dbContextModel.addAll(dbContextItems)
            }
        }
    }

    protected open fun initData() {
        initPreferredProjects()
        if (settingsStateService.usePreviouslySelectedOptionsInDialogs) {
            commonCtx.loadState(dialogsStateService.forDialog(COMMON_DIALOG_ID))
            loadDialogState(dialogsStateService.forDialog(dialogId))
        }
    }

    private fun initPreferredProjects() {
        if (isSolutionLevelMode) {
            initSolutionLevelPreferredProjects()
        } else {
            initProjectLevelPreferredProjects()
        }
    }

    private fun initSolutionLevelPreferredProjects() {
        val (preferredMigrationsProject, preferredStartupProject) =
            preferredProjectsManager.getGlobalProjectPair(
                commonCtx.availableMigrationsProjects,
                commonCtx.availableStartupProjects)

        refreshProjectsPair(preferredMigrationsProject, preferredStartupProject)
    }

    private fun initProjectLevelPreferredProjects() {
        val migrationsProjects = commonCtx.availableMigrationsProjects
        val startupProjects = commonCtx.availableStartupProjects

        val selectedDotnetProject =
            migrationsProjects.find { it.name == selectedDotnetProjectName }

        val (preferredMigrationsProject, preferredStartupProject) =
            preferredProjectsManager.getProjectPair(selectedDotnetProject?.id, migrationsProjects, startupProjects)

        refreshProjectsPair(preferredMigrationsProject, preferredStartupProject)
    }

    private fun refreshProjectsPair(migrationsProject: MigrationsProjectInfo?, startupProject: StartupProjectInfo?) {
        commonCtx.migrationsProject.value = migrationsProject
        commonCtx.startupProject.value = startupProject
    }

    protected open fun loadDialogState(dialogState: DialogsStateService.SpecificDialogState) {}

    protected open fun saveDialogState(dialogState: DialogsStateService.SpecificDialogState) {}

    //
    // Methods
    override fun doOKAction() {
        super.doOKAction()

        val migrationsProject = commonCtx.migrationsProject.value
        val startupProject = commonCtx.startupProject.value

        if (migrationsProject != null && startupProject != null) {
            if (isSolutionLevelMode) {
                preferredProjectsManager.setGlobalProjectPair(migrationsProject, startupProject)
            } else {
                preferredProjectsManager.setProjectPair(migrationsProject, startupProject)
            }

            commonCtx.saveState(dialogsStateService.forDialog(COMMON_DIALOG_ID))
            saveDialogState(dialogsStateService.forDialog(dialogId))
        }
    }

    //
    // Command lifetime
    abstract fun generateCommand(): CliCommand

    open fun postCommandExecute(commandResult: CliCommandResult) {}

    //
    // UI
    override fun createCenterPanel(): JComponent =
        createMainUI()
            .apply {
                panel = this
            }

    override fun createLeftSideActions(): Array<Action> {
        val commandPreviewAction = object : AbstractAction("Preview") {
            override fun actionPerformed(e: ActionEvent?) {
                applyFields()
                contentPanel
                if (panel.validateAll().isEmpty()) {
                    val dialog = CommandPreviewDialogWrapper(generateCommand())
                    dialog.show()
                }
            }
        }

        return arrayOf(commandPreviewAction)
    }

    protected fun createMainUI(): DialogPanel {
        return panel {
            panel {
                groupRowsRange("Common") {
                    createPrimaryOptions()
                    createDefaultMainRows()
                }
                panel {
                    createAdditionalGroup()
                    createBuildOptions()
                    createExecutionRow()
                }
            }
        }
    }

    protected open fun Panel.createDefaultMainRows() {
        createMigrationsProjectRow()
        createStartupProjectRow()
        if (requireDbContext) {
            createDbContextProjectRow()
        }
    }

    protected fun Panel.createMigrationsProjectRow() {
        row("Migrations project:") {
            iconComboBox(migrationsProjectsModel, selectedMigrationsProject)
                .validationOnInput(validator.migrationsProjectValidation())
                .validationOnApply(validator.migrationsProjectValidation())
        }
    }

    protected fun Panel.createStartupProjectRow() {
        row("Startup project:") {
            iconComboBox(startupProjectsModel, selectedStartupProject)
                .validationOnInput(validator.startupProjectValidation())
                .validationOnApply(validator.startupProjectValidation())
                .comment(
                    "Your project is not listed? " +
                    "<a href='https://plugins.jetbrains.com/plugin/18147-entity-framework-core-ui/f-a-q#why-i-cant-see-my-project-in-a-startup-projects-field'>" +
                        "Help" +
                    "</a>")
        }
    }

    protected fun Panel.createDbContextProjectRow() {
        row("DbContext class:") {
            iconComboBox(dbContextModel, selectedDbContext)
                .validationOnInput(validator.dbContextValidation())
                .validationOnApply(validator.dbContextValidation())
        }
    }

    protected open fun Panel.createPrimaryOptions() {}

    protected open fun Panel.createAdditionalGroup() {}

    protected fun Panel.createBuildOptions() {
        groupRowsRange("Build Options") {
            var noBuildCheck: JBCheckBox? = null
            row {
                noBuildCheck = checkBox("Skip project build process (<code>--no-build</code>)")
                    .bindSelected(commonCtx.noBuild)
                    .component
            }

            row("Build configuration:") {
                iconComboBox(buildConfigurationModel, selectedBuildConfiguration)
                    .validationOnInput(validator.buildConfigurationValidation())
                    .validationOnApply(validator.buildConfigurationValidation())
            }.enabledIf(noBuildCheck!!.selected.not())

            row("Target framework:") {
                iconComboBox(targetFrameworkModel, selectedTargetFramework)
                    .validationOnInput(validator.targetFrameworkValidation())
                    .validationOnInput(validator.targetFrameworkValidation())
            }.enabledIf(noBuildCheck!!.selected.not())
        }
    }

    protected fun Panel.createExecutionRow() {
        val toolLabel =
            if (beModel.efToolsDefinition.hasValue) {
                val efToolsDefinition = beModel.efToolsDefinition.valueOrNull!!
                when (efToolsDefinition.toolKind) {
                    ToolKind.None -> "None"
                    else -> "${efToolsDefinition.toolKind.name}, ${efToolsDefinition.version}"
                }
            } else "None"

        groupRowsRange("Execution") {
            if (efCoreVersion.major >= 5) {
                row("Additional arguments:") {
                    simpleExpandableTextField(commonCtx.additionalArguments)
                        .horizontalAlign(HorizontalAlign.FILL)
                }
            }
            row("EF Core tools:") {
                label(toolLabel)
            }
        }
    }

    //
    // Helpers
    protected fun getCommonOptions(): CommonOptions = CommonOptions(
        commonCtx.migrationsProject.notNullValue.fullPath,
        commonCtx.startupProject.notNullValue.fullPath,
        commonCtx.dbContext.value?.fullName,
        commonCtx.buildConfiguration.notNullValue,
        commonCtx.targetFramework.notNullValue,
        commonCtx.noBuild.notNullValue,
        commonCtx.additionalArguments.notNullValue
    )

    companion object {
        val COMMON_DIALOG_ID = "Common"

        private fun DbContextInfo.toViewItem() =
            DbContextItem(name, fullName)

        private fun String?.toTargetFrameworkViewItem() =
            if (this == null) DefaultTargetFrameworkItem()
            else TargetFrameworkItem(this, this)
    }
}

