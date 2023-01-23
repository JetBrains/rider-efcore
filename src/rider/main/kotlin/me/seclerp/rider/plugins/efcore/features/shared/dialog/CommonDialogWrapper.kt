package me.seclerp.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.jetbrains.rd.util.reactive.hasValue
import com.jetbrains.rider.projectView.solution
import me.seclerp.observables.bind
import me.seclerp.observables.observable
import me.seclerp.observables.observableList
import me.seclerp.observables.withLogger
import me.seclerp.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import me.seclerp.rider.plugins.efcore.cli.execution.CommonOptions
import me.seclerp.rider.plugins.efcore.features.preview.CommandPreviewDialogWrapper
import me.seclerp.rider.plugins.efcore.features.shared.services.PreferredProjectsManager
import me.seclerp.rider.plugins.efcore.rd.*
import me.seclerp.rider.plugins.efcore.settings.EfCoreUiSettingsStateService
import me.seclerp.rider.plugins.efcore.state.DialogsStateService
import me.seclerp.observables.ui.dsl.bindSelected
import me.seclerp.observables.ui.dsl.iconComboBox
import me.seclerp.rider.plugins.efcore.ui.items.*
import me.seclerp.rider.plugins.efcore.ui.simpleExpandableTextField
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

@Suppress("MemberVisibilityCanBePrivate")
abstract class CommonDialogWrapper<TContext : CommonDataContext>(
    protected val dataCtx: TContext,
    protected val efCoreVersion: DotnetEfVersion,
    dialogTitle: String,
    protected val intellijProject: Project,
    private val selectedProjectId: UUID?,
    requireMigrationsInProject: Boolean = false
) : BaseDialogWrapper() {

    private val dialogId = dialogTitle.replace(" ", "")

    protected val beModel = intellijProject.solution.riderEfCoreModel

    //
    // Internal data
    private val availableMigrationsProjectsView = observableList<MigrationsProjectItem?>()
    private val availableStartupProjectsView = observableList<StartupProjectItem?>()
    private val availableDbContextsView = observableList<DbContextItem?>()
    private val availableTargetFrameworksView = observableList<BaseTargetFrameworkItem?>()
    private val availableBuildConfigurationView = observableList<BuildConfigurationItem?>()

    private val migrationsProjectView = observable<MigrationsProjectItem?>(null).withLogger("migrationsProjectView")
    private val startupProjectView = observable<StartupProjectItem?>(null).withLogger("startupProjectView")
    private val dbContextView = observable<DbContextItem?>(null).withLogger("dbContextView")
    private val targetFrameworksView = observable<BaseTargetFrameworkItem?>(null).withLogger("targetFrameworksView")
    private val buildConfigurationView = observable<BuildConfigurationItem?>(null).withLogger("buildConfigurationView")

    private val isSolutionLevelMode = selectedProjectId == null

    //
    // Validation
    private val validator = CommonDialogValidator(dataCtx, beModel, intellijProject, requireMigrationsInProject)

    //
    // Preferences
    private val preferredProjectsManager = intellijProject.service<PreferredProjectsManager>()
    private val settingsStateService = EfCoreUiSettingsStateService.getInstance()
    private val dialogsStateService = intellijProject.service<DialogsStateService>()

    //
    // UI
    protected lateinit var panel: DialogPanel

    //
    // Constructor
    init {
        title = dialogTitle
    }

    protected fun initUi() {
        init()
        initBindings()
        initData()
    }

    protected open fun initBindings() {
        dataCtx.initBindings()
        initProjectsBindings()
        initDbContextBindings()
        initTargetFrameworkBindings()
        initBuildConfigurationBindings()
    }

    private fun initProjectsBindings() {
        availableStartupProjectsView.bind(dataCtx.availableStartupProjects) {
            it.map(mappings.startupProject.toItem)
        }

        // Startup project bindings
        startupProjectView.bind(dataCtx.startupProject,
            mappings.startupProject.toItem,
            mappings.startupProject.fromItem)

        availableMigrationsProjectsView.bind(dataCtx.availableMigrationsProjects) {
            it.map(mappings.migrationsProject.toItem)
        }

        if (settingsStateService.usePreviouslySelectedOptionsInDialogs) {
            // Since some properties are persisted depending on the selected project, we should reload them once
            // project changes
            dataCtx.startupProject.afterChange {
                dataCtx.loadState(dialogsStateService.forDialog(COMMON_DIALOG_ID))
            }
        }

        // Migration projects bindings
        migrationsProjectView.bind(dataCtx.migrationsProject,
            mappings.migrationsProject.toItem,
            mappings.migrationsProject.fromItem)

        if (settingsStateService.usePreviouslySelectedOptionsInDialogs) {
            // Since some properties are persisted depending on the selected project, we should reload them once
            // project changes
            dataCtx.migrationsProject.afterChange {
                dataCtx.loadState(dialogsStateService.forDialog(COMMON_DIALOG_ID))
            }
        }
    }

    private fun initDbContextBindings() {
        availableDbContextsView.bind(dataCtx.availableDbContexts) {
            it.map(mappings.dbContext.toItem)
        }

        dbContextView.bind(availableDbContextsView) { it.firstOrNull() }

        dbContextView.bind(dataCtx.dbContext,
            mappings.dbContext.toItem,
            mappings.dbContext.fromItem)
    }

    private fun initBuildConfigurationBindings() {
        availableBuildConfigurationView.bind(dataCtx.availableBuildConfigurations) {
            it.map(mappings.buildConfiguration.toItem)
        }

        buildConfigurationView.bind(dataCtx.buildConfiguration,
            mappings.buildConfiguration.toItem,
            mappings.buildConfiguration.fromItem)
    }

    private fun initTargetFrameworkBindings() {
        availableTargetFrameworksView.bind(dataCtx.availableTargetFrameworks) {
            it.map(mappings.targetFramework.toItem)
        }

        targetFrameworksView.bind(dataCtx.targetFramework,
            mappings.targetFramework.toItem,
            mappings.targetFramework.fromItem)
    }

    protected open fun initData() {
        dataCtx.initData()

        initPreferredProjects()

        if (settingsStateService.usePreviouslySelectedOptionsInDialogs) {
            dataCtx.loadState(dialogsStateService.forDialog(COMMON_DIALOG_ID))
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
                dataCtx.availableMigrationsProjects,
                dataCtx.availableStartupProjects)

        refreshProjectsPair(preferredMigrationsProject, preferredStartupProject)
    }

    private fun initProjectLevelPreferredProjects() {
        val migrationsProjects = dataCtx.availableMigrationsProjects
        val startupProjects = dataCtx.availableStartupProjects

        val selectedDotnetProject =
            migrationsProjects.find { it.id == selectedProjectId }

        val (preferredMigrationsProject, preferredStartupProject) =
            preferredProjectsManager.getProjectPair(selectedDotnetProject?.id, migrationsProjects, startupProjects)

        refreshProjectsPair(preferredMigrationsProject, preferredStartupProject)
    }

    private fun refreshProjectsPair(migrationsProject: MigrationsProjectInfo?, startupProject: StartupProjectInfo?) {
        dataCtx.migrationsProject.value = migrationsProject
        dataCtx.startupProject.value = startupProject
    }

    //
    // Methods
    override fun doOKAction() {
        super.doOKAction()

        val migrationsProject = dataCtx.migrationsProject.value
        val startupProject = dataCtx.startupProject.value

        if (migrationsProject != null && startupProject != null) {
            if (isSolutionLevelMode) {
                preferredProjectsManager.setGlobalProjectPair(migrationsProject, startupProject)
            } else {
                preferredProjectsManager.setProjectPair(migrationsProject, startupProject)
            }

            dataCtx.saveState(dialogsStateService.forDialog(COMMON_DIALOG_ID))
        }
    }

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
        if (dataCtx.requireDbContext) {
            createDbContextProjectRow()
        }
    }

    protected fun Panel.createMigrationsProjectRow() {
        row("Migrations project:") {
            iconComboBox(migrationsProjectView, availableMigrationsProjectsView)
                .validationOnInput(validator.migrationsProjectValidation())
                .validationOnApply(validator.migrationsProjectValidation())
        }
    }

    protected fun Panel.createStartupProjectRow() {
        row("Startup project:") {
            iconComboBox(startupProjectView, availableStartupProjectsView)
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
            iconComboBox(dbContextView, availableDbContextsView)
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
                noBuildCheck = checkBox("Skip project build process (--no-build)")
                    .bindSelected(dataCtx.noBuild)
                    .component
            }

            row("Build configuration:") {
                iconComboBox(buildConfigurationView, availableBuildConfigurationView)
                    .validationOnInput(validator.buildConfigurationValidation())
                    .validationOnApply(validator.buildConfigurationValidation())
            }.enabledIf(noBuildCheck!!.selected.not())

            row("Target framework:") {
                iconComboBox(targetFrameworksView, availableTargetFrameworksView)
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
                    simpleExpandableTextField(dataCtx.additionalArguments)
                        .align(AlignX.FILL)
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
        dataCtx.migrationsProject.value!!.fullPath,
        dataCtx.startupProject.value!!.fullPath,
        dataCtx.dbContext.value?.fullName,
        dataCtx.buildConfiguration.value!!,
        dataCtx.targetFramework.value,
        dataCtx.noBuild.value,
        dataCtx.additionalArguments.value
    )

    companion object {
        val COMMON_DIALOG_ID = "Common"

        private object mappings {
            object migrationsProject {
                val toItem: (MigrationsProjectInfo?) -> MigrationsProjectItem?
                    get() = {
                        if (it == null) null else MigrationsProjectItem(it.name, it)
                    }

                val fromItem: (MigrationsProjectItem?) -> MigrationsProjectInfo?
                    get() = { it?.data }
            }

            object startupProject {
                val toItem: (StartupProjectInfo?) -> StartupProjectItem?
                    get() = {
                        if (it == null) null else StartupProjectItem(it.name, it)
                    }

                val fromItem: (StartupProjectItem?) -> StartupProjectInfo?
                    get() = { it?.data }
            }

            object dbContext {
                val toItem: (DbContextInfo?) -> DbContextItem?
                    get() = {
                        if (it == null) null else DbContextItem(it.name, it)
                    }

                val fromItem: (DbContextItem?) -> DbContextInfo?
                    get() = { it?.data }
            }

            object targetFramework {
                val toItem: (String?) -> BaseTargetFrameworkItem?
                    get() = { it.toTargetFrameworkViewItem() }

                val fromItem: (BaseTargetFrameworkItem?) -> String?
                    get() = { it?.data }
            }

            object buildConfiguration {
                val toItem: (String?) -> BuildConfigurationItem?
                    get() = {
                        if (it == null) null else BuildConfigurationItem(it)
                    }

                val fromItem: (BuildConfigurationItem?) -> String?
                    get() = { it?.displayName }
            }
        }

        private fun String?.toTargetFrameworkViewItem() =
            if (this != null)
                TargetFrameworkItem(this, this)
            else
                DefaultTargetFrameworkItem()
    }
}

