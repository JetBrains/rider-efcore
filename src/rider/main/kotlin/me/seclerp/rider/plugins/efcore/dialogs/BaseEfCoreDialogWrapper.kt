package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.*
import com.jetbrains.rd.ui.bedsl.extensions.valueOrEmpty
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.iconComboBox
import me.seclerp.rider.plugins.efcore.components.items.*
import me.seclerp.rider.plugins.efcore.models.MigrationsProjectData
import me.seclerp.rider.plugins.efcore.models.StartupProjectData
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import java.util.*
import javax.swing.DefaultComboBoxModel

abstract class BaseEfCoreDialogWrapper(
    title: String,
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    private val actionDotnetProjectName: String,
    private val shouldHaveMigrationsInProject: Boolean = false
): DialogWrapper(true) {
    var migrationsProject: MigrationsProjectItem? = null
        private set

    var startupProject: StartupProjectItem? = null
        private set

    var dbContext: DbContextItem? = null
        private set

    var buildConfiguration: BuildConfigurationItem? = null
        private set

    var targetFramework: TargetFrameworkItem? = null
        private set

    var noBuild = false
        private set

    private val migrationsProjects: Array<MigrationsProjectItem>
    private val startupProjects: Array<StartupProjectItem>
    private val dotnetProjectName: String?
    private val dotnetProjectId: UUID?

    @Suppress("MemberVisibilityCanBePrivate")
    protected val migrationsProjectChangedEvent: Event<MigrationsProjectItem> = Event()

    @Suppress("MemberVisibilityCanBePrivate")
    protected val startupProjectChangedEvent: Event<StartupProjectItem> = Event()

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dbContextChangedEvent: Event<DbContextItem?> = Event()

    private var targetFrameworkModel: DefaultComboBoxModel<TargetFrameworkItem>
    private var dbContextModel: DefaultComboBoxModel<DbContextItem>

    private lateinit var noBuildCheckbox: JBCheckBox
    private lateinit var dbContextBox: ComboBox<DbContextItem>
    private lateinit var buildConfigurationModel: DefaultComboBoxModel<BuildConfigurationItem>

    private var prevPreferredMigrationsProjectId: UUID? = null
    private var prevPreferredStartupProjectId: UUID? = null

    init {
        this.title = title

        migrationsProjects = model.getAvailableMigrationsProjects
            .sync(Unit)
            .map { MigrationsProjectItem(it.name, MigrationsProjectData(it.id, it.fullPath)) }
            .toTypedArray()

        startupProjects = model.getAvailableStartupProjects
            .sync(Unit)
            .map { StartupProjectItem(it.name, StartupProjectData(it.id, it.fullPath, it.targetFrameworks)) }
            .toTypedArray()

        val dotnetProject = migrationsProjects.find { it.displayName == actionDotnetProjectName }
            ?: migrationsProjects.firstOrNull()

        dotnetProjectName = dotnetProject?.displayName
        dotnetProjectId = dotnetProject?.data?.id

        migrationsProjectChangedEvent += ::migrationsProjectChanged
        startupProjectChangedEvent += ::startupProjectChanged

        targetFrameworkModel = DefaultComboBoxModel<TargetFrameworkItem>()
        dbContextModel = DefaultComboBoxModel<DbContextItem>()
    }

    override fun createCenterPanel(): DialogPanel = panel {
        primaryOptions(this)
        additionalOptions(this)
    }

    override fun doOKAction() {
        super.doOKAction()

        val commonOptionsService = CommonOptionsStateService.getInstance(intellijProject)

        if (prevPreferredMigrationsProjectId != null && prevPreferredStartupProjectId != null)
            commonOptionsService.clearPreferredProjectsPair(prevPreferredMigrationsProjectId!!, prevPreferredStartupProjectId!!)

        commonOptionsService.setPreferredProjectsPair(migrationsProject!!.data.id, startupProject!!.data.id)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun primaryOptions(parent: LayoutBuilder, customOptions: LayoutBuilder.() -> Unit) {
        customOptions(parent)
        loadPreferredProjects()
        migrationsProjectRow(parent)
        startupProjectRow(parent)
        dbContextRow(parent)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun primaryOptions(parent: LayoutBuilder) = primaryOptions(parent) { }

    @Suppress("MemberVisibilityCanBePrivate")
    fun additionalOptions(parent: LayoutBuilder, customOptions: Row.() -> Unit) {
        parent.titledRow("Additional Options") {
            customOptions(this)
        }

        buildOptionsRow(parent)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun additionalOptions(parent: LayoutBuilder) {
        buildOptionsRow(parent)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun migrationsProjectRow(parent: LayoutBuilder): Row {
        val migrationsBoxModel = DefaultComboBoxModel(migrationsProjects)

        return parent.row("Migrations project:") {
            iconComboBox(migrationsBoxModel, { migrationsProject }, ::migrationsProjectSetter)
                .withValidationOnInput(migrationsProjectValidation())
                .withValidationOnApply(migrationsProjectValidation())
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun startupProjectRow(parent: LayoutBuilder): Row {
        val startupBoxModel = DefaultComboBoxModel(startupProjects)

        return parent.row("Startup project:") {
            iconComboBox(startupBoxModel, { startupProject }, ::startupProjectSetter)
                .withValidationOnInput(startupProjectValidation())
                .withValidationOnApply(startupProjectValidation())
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun dbContextRow(parent: LayoutBuilder): Row {
        return parent.row("DbContext class:") {
            dbContextBox = iconComboBox(dbContextModel, { dbContext }, ::dbContextSetter)
                .withValidationOnInput(dbContextValidation())
                .withValidationOnApply(dbContextValidation())
                .component
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun buildOptionsRow(parent: LayoutBuilder) =
        parent.titledRow("Build Options") {
            noBuildRow(this)

            buildConfigurationRow(this)
                .enableIf(noBuildCheckbox.selected.not())

            targetFrameworkRow(this)
                .enableIf(noBuildCheckbox.selected.not())
        }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun noBuildRow(parent: Row) =
        parent.row {
            noBuildCheckbox = checkBox("Skip project build process (--no-build)", { noBuild }, { noBuild = it }).component
        }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun buildConfigurationRow(parent: Row): Row {
        val currentConfiguration = intellijProject.solution.solutionProperties.activeConfigurationPlatform.value

        val availableConfigurations = intellijProject.solution.solutionProperties.configurationsAndPlatformsCollection
            .valueOrEmpty()
            .distinctBy { it.configuration } // To get around of different platforms for the same configurations
            .map { BuildConfigurationItem(it.configuration) }
            .toTypedArray()

        buildConfigurationModel = DefaultComboBoxModel(availableConfigurations)
        buildConfiguration = availableConfigurations.find { it.displayName == currentConfiguration?.configuration }
            ?: availableConfigurations.firstOrNull()

        return parent.row("Build configuration:") {
            iconComboBox(buildConfigurationModel, { buildConfiguration }, ::buildConfigurationSetter)
                .withValidationOnInput(buildConfigurationValidation())
                .withValidationOnApply(buildConfigurationValidation())
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun targetFrameworkRow(parent: Row): Row {

        return parent.row("Target framework:") {
            iconComboBox(targetFrameworkModel, { targetFramework }, ::targetFrameworkSetter)
                .withValidationOnInput(targetFrameworkValidation())
                .withValidationOnApply(targetFrameworkValidation())
        }
    }

    private fun loadPreferredProjects() {
        if (dotnetProjectId == null || dotnetProjectName == null) {
            setDefaultProjects()
            return
        }

        val preferredProjects = CommonOptionsStateService.getInstance(intellijProject).getPreferredProjectPair(dotnetProjectId)
        if (preferredProjects != null) {
            val (migrationsProjectId, startupProjectId) = preferredProjects
            prevPreferredMigrationsProjectId = migrationsProjectId
            prevPreferredStartupProjectId = startupProjectId
            val migrationsProject = migrationsProjects.find { it.data.id == migrationsProjectId } ?: migrationsProjects.firstOrNull()
            val startupProject = startupProjects.find { it.data.id == startupProjectId } ?: startupProjects.firstOrNull()

            migrationsProjectSetter(migrationsProject)
            startupProjectSetter(startupProject)
        } else {
            setDefaultProjects()
        }
    }

    private fun setDefaultProjects() {
        migrationsProjectSetter(migrationsProjects.find { it.displayName == dotnetProjectName } ?: migrationsProjects.firstOrNull())
        startupProjectSetter(startupProjects.find { it.displayName == dotnetProjectName } ?: startupProjects.firstOrNull())
    }

    private fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<MigrationsProjectItem>) -> ValidationInfo? = {
        if (migrationsProject == null)
            error("You should selected valid migrations project")
        else if (shouldHaveMigrationsInProject) {
            if (dbContext == null)
                null
            else {
                val migrationsIdentity = MigrationsIdentity(migrationsProject!!.displayName, dbContext!!.data)
                val hasMigrations = model.hasAvailableMigrations.runUnderProgress(migrationsIdentity, intellijProject, "Checking migrations...",
                    isCancelable = true,
                    throwFault = true
                )

                if (hasMigrations == null || !hasMigrations)
                    error("Selected migrations project doesn't have migrations")
                else
                    null
            }
        } else null
    }

    private fun startupProjectValidation(): ValidationInfoBuilder.(ComboBox<StartupProjectItem>) -> ValidationInfo? = {
        if (startupProject == null)
            error("You should selected valid startup project")
        else
            null
    }

    private fun dbContextValidation(): ValidationInfoBuilder.(ComboBox<DbContextItem>) -> ValidationInfo? = {
        if (dbContext == null || dbContextModel.size == 0)
            error("Migrations project should have at least 1 DbContext")
        else
            null
    }

    private fun buildConfigurationValidation(): ValidationInfoBuilder.(ComboBox<BuildConfigurationItem>) -> ValidationInfo? = {
        if (buildConfiguration == null || buildConfigurationModel.size == 0)
            error("Solution doesn't have any build configurations")
        else
            null
    }

    private fun targetFrameworkValidation(): ValidationInfoBuilder.(ComboBox<TargetFrameworkItem>) -> ValidationInfo? = {
        if (targetFramework == null || targetFrameworkModel.size == 0)
            error("Startup project should have at least 1 supported target framework")
        else
            null
    }

    private fun migrationsProjectSetter(project: MigrationsProjectItem?) {
        if (project == migrationsProject) return

        migrationsProject = project
        migrationsProjectChangedEvent.invoke(migrationsProject!!)
    }

    private fun startupProjectSetter(project: StartupProjectItem?) {
        if (project == startupProject) return

        startupProject = project
        startupProjectChangedEvent.invoke(startupProject!!)
    }

    private fun dbContextSetter(context: DbContextItem?) {
        if (context == dbContext) return

        dbContext = context
        dbContextChangedEvent.invoke(dbContext)
    }

    private fun buildConfigurationSetter(configuration: BuildConfigurationItem?) {
        if (configuration == buildConfiguration) return

        buildConfiguration = configuration
    }

    private fun targetFrameworkSetter(framework: TargetFrameworkItem?) {
        if (framework == targetFramework) return

        targetFramework = framework
    }

    private fun migrationsProjectChanged(project: MigrationsProjectItem?) {
        dbContextModel.removeAllElements()

        if (project == null) return

        val dbContexts = model.getAvailableDbContexts.runUnderProgress(migrationsProject!!.displayName, intellijProject, "Loading DbContext classes...",
            isCancelable = true,
            throwFault = true
        )

        val dbContextIconItems = dbContexts!!.map { DbContextItem(it.name, it.fullName) }

        dbContextModel.addAll(dbContextIconItems)
        val firstDbContext = dbContextIconItems.firstOrNull()
        dbContextSetter(firstDbContext)

        if (::dbContextBox.isInitialized)
            dbContextBox.item = dbContext
    }

    private fun startupProjectChanged(project: IconItem<StartupProjectData>?) {
        targetFrameworkModel.removeAllElements()

        if (project == null) return

        val configurationIconItems = project.data.targetFrameworks
            .map { TargetFrameworkItem(it) }

        targetFrameworkModel.addAll(configurationIconItems)
        targetFramework = configurationIconItems.firstOrNull()
        targetFrameworkModel.selectedItem = targetFramework
    }
}