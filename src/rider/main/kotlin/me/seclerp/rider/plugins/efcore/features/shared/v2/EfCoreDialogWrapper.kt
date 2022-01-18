package me.seclerp.rider.plugins.efcore.features.shared.v2

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.jetbrains.rd.ui.bedsl.extensions.valueOrEmpty
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.features.shared.models.MigrationsProjectData
import me.seclerp.rider.plugins.efcore.features.shared.models.StartupProjectData
import me.seclerp.rider.plugins.efcore.features.shared.services.PreferredProjectsManager
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import me.seclerp.rider.plugins.efcore.ui.iconComboBox
import me.seclerp.rider.plugins.efcore.ui.items.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

@Suppress("UnstableApiUsage", "MemberVisibilityCanBePrivate")
abstract class EfCoreDialogWrapper(
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    private val selectedDotnetProjectName: String,
    shouldHaveMigrationsInProject: Boolean = false
) : DialogWrapper(true) {
    //
    // Data binding
    val commonOptions = CommonOptionsModel()

    //
    // Internal data
    private val availableMigrationsProjects =
        beModel.getAvailableMigrationsProjects
            .sync(Unit)
            .map { MigrationsProjectItem(it.name, MigrationsProjectData(it.id, it.fullPath)) }
            .toTypedArray()

    private val availableStartupProjects =
        beModel.getAvailableStartupProjects
            .sync(Unit)
            .map { StartupProjectItem(it.name, StartupProjectData(it.id, it.fullPath, it.targetFrameworks)) }
            .toTypedArray()

    private val availableBuildConfigurations =
        intellijProject.solution.solutionProperties.configurationsAndPlatformsCollection
            .valueOrEmpty()
            .distinctBy { it.configuration } // To get around of different platforms for the same configurations
            .map { BuildConfigurationItem(it.configuration) }
            .toTypedArray()

    private var targetFrameworkModel: DefaultComboBoxModel<BaseTargetFrameworkItem> = DefaultComboBoxModel()
    private var dbContextModel: DefaultComboBoxModel<DbContextItem> = DefaultComboBoxModel()

    //
    // Events
    private val migrationsProjectChangedEvent: Event<MigrationsProjectItem> = Event()
    private val startupProjectChangedEvent: Event<StartupProjectItem> = Event()
    private val dbContextChangedEvent: Event<DbContextItem?> = Event()

    //
    // Validation
    private val validator = EfCoreDialogValidator(
        commonOptions, beModel, intellijProject, shouldHaveMigrationsInProject, dbContextModel,
        availableBuildConfigurations, targetFrameworkModel)

    //
    // Preferences
    private val preferredProjectsManager = PreferredProjectsManager(intellijProject)

    //
    // Constructor
    init {
        addMigrationsProjectChangedListener(::migrationsProjectChanged)
        addStartupProjectChangedListener(::startupProjectChanged)

        initSelectedBuildConfiguration()
        initPreferredProjects()
    }

    private fun initSelectedBuildConfiguration() {
        val currentBuilderConfiguration = intellijProject.solution.solutionProperties.activeConfigurationPlatform.value

        commonOptions.buildConfiguration =
            availableBuildConfigurations.find {
                it.displayName == currentBuilderConfiguration?.configuration
            } ?: availableBuildConfigurations.firstOrNull()
    }

    private fun initPreferredProjects() {
        val selectedDotnetProject =
            availableMigrationsProjects.find { it.displayName == selectedDotnetProjectName }

        val (preferredMigrationsProject, preferredStartupProject) =
            preferredProjectsManager.getProjectPair(selectedDotnetProject?.data?.id, availableMigrationsProjects, availableStartupProjects)

        migrationsProjectSetter(preferredMigrationsProject)
        startupProjectSetter(preferredStartupProject)
    }

    //
    // Methods
    protected fun addMigrationsProjectChangedListener(listener: (MigrationsProjectItem) -> Unit) {
        migrationsProjectChangedEvent += listener
    }

    protected fun addStartupProjectChangedListener(listener: (StartupProjectItem) -> Unit) {
        startupProjectChangedEvent += listener
    }

    protected fun addDbContextChangedListener(listener: (DbContextItem?) -> Unit) {
        dbContextChangedEvent += listener
    }

    override fun doOKAction() {
        super.doOKAction()

        val migrationsProject = commonOptions.migrationsProject
        val startupProject = commonOptions.startupProject

        if (migrationsProject != null && startupProject != null) {
            preferredProjectsManager.setProjectPair(migrationsProject, startupProject)
        }
    }

    override fun createCenterPanel(): JComponent? =
        panel {
            createPrimaryGroup()(this)
            createSecondaryGroup()(this)
        }

    protected open fun createPrimaryGroup(): Panel.() -> Panel = {
        panel {
            createMigrationsProjectRow()(this)
            createStartupProjectRow()(this)
            createDbContextProjectRow()(this)
        }
    }

    protected fun createMigrationsProjectRow(): Panel.() -> Row = {
        row("Migrations project:") {
            iconComboBox(availableMigrationsProjects, { commonOptions.migrationsProject }, ::migrationsProjectSetter)
                .validationOnInput(validator.migrationsProjectValidation())
                .validationOnApply(validator.migrationsProjectValidation())
        }
    }

    protected fun createStartupProjectRow(): Panel.() -> Row = {
        row("Startup project:") {
            iconComboBox(availableStartupProjects, { commonOptions.startupProject }, ::startupProjectSetter)
                .validationOnInput(validator.startupProjectValidation())
                .validationOnApply(validator.startupProjectValidation())
        }
    }

    protected fun createDbContextProjectRow(): Panel.() -> Row = {
        row("DbContext class:") {
            iconComboBox(dbContextModel, { commonOptions.dbContext }, ::dbContextSetter)
                .validationOnInput(validator.dbContextValidation())
                .validationOnApply(validator.dbContextValidation())
        }
    }

    protected open fun createSecondaryGroup(): Panel.() -> Panel = {
        createAdditionalOptions()(this)
        createBuildOptions()(this)
    }

    protected open fun createAdditionalOptions(): Panel.() -> Panel = {
        group("Additional Options") { }
    }

    protected fun createBuildOptions(): Panel.() -> Panel = {
        group("Build Options") {
            var noBuildCheck: JBCheckBox? = null
            row {
                noBuildCheck = checkBox("Skip project build process (--no-build)")
                    .bindSelected(commonOptions::noBuild)
                    .component
            }

            row("Build configuration:") {
                iconComboBox(availableBuildConfigurations, { commonOptions.buildConfiguration }, ::buildConfigurationSetter)
                    .validationOnInput(validator.buildConfigurationValidation())
                    .validationOnApply(validator.buildConfigurationValidation())
            }.enabledIf(noBuildCheck!!.selected.not())

            row("Target framework:") {
                iconComboBox(targetFrameworkModel, { commonOptions.targetFramework }, ::targetFrameworkSetter)
                    .validationOnInput(validator.targetFrameworkValidation())
                    .validationOnInput(validator.targetFrameworkValidation())
            }.enabledIf(noBuildCheck!!.selected.not())
        }
    }

    //
    // Setters
    private fun migrationsProjectSetter(project: MigrationsProjectItem?) {
        if (project == commonOptions.migrationsProject) return

        commonOptions.migrationsProject = project
        migrationsProjectChangedEvent.invoke(commonOptions.migrationsProject!!)
    }

    private fun startupProjectSetter(project: StartupProjectItem?) {
        if (project == commonOptions.startupProject) return

        commonOptions.startupProject = project
        startupProjectChangedEvent.invoke(commonOptions.startupProject!!)
    }

    private fun dbContextSetter(context: DbContextItem?) {
        if (context == commonOptions.dbContext) return

        commonOptions.dbContext = context
        dbContextChangedEvent.invoke(commonOptions.dbContext)
    }

    private fun buildConfigurationSetter(configuration: BuildConfigurationItem?) {
        if (configuration == commonOptions.buildConfiguration) return

        commonOptions.buildConfiguration = configuration
    }

    private fun targetFrameworkSetter(framework: BaseTargetFrameworkItem?) {
        if (framework == commonOptions.targetFramework) return

        commonOptions.targetFramework = framework
    }

    //
    // Event listeners
    private fun migrationsProjectChanged(project: MigrationsProjectItem?) {
        dbContextModel.removeAllElements()

        if (project == null) return

        val dbContexts = beModel.getAvailableDbContexts.runUnderProgress(
            commonOptions.migrationsProject!!.displayName, intellijProject, "Loading DbContext classes...",
            isCancelable = true,
            throwFault = true
        )

        val dbContextIconItems = dbContexts!!.map { DbContextItem(it.name, it.fullName) }

        dbContextModel.addAll(dbContextIconItems)
        val firstDbContext = dbContextIconItems.firstOrNull()
        dbContextSetter(firstDbContext)
    }

    private fun startupProjectChanged(project: IconItem<StartupProjectData>?) {
        targetFrameworkModel.removeAllElements()

        if (project == null) return

        val baseTargetFrameworkItems = project.data.targetFrameworks
            .map { TargetFrameworkItem(it, it) } as List<BaseTargetFrameworkItem>

        val defaultFramework = DefaultTargetFrameworkItem()

        targetFrameworkModel.addElement(defaultFramework)
        targetFrameworkModel.addAll(baseTargetFrameworkItems)
        commonOptions.targetFramework = defaultFramework
        targetFrameworkModel.selectedItem = commonOptions.targetFramework
    }
}

