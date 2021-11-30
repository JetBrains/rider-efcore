package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.*
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.DotnetIconType
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.IconItem
import me.seclerp.rider.plugins.efcore.components.iconComboBox
import me.seclerp.rider.plugins.efcore.models.StartupProjectData
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.rd.toIconItem
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import javax.swing.DefaultComboBoxModel

abstract class BaseEfCoreDialogWrapper(
    title: String,
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    private val currentDotnetProjectName: String,
    private val shouldHaveMigrationsInProject: Boolean = false
): DialogWrapper(true) {
    var migrationsProject: IconItem<String>? = null
        private set

    var startupProject: IconItem<StartupProjectData>? = null
        private set

    var dbContext: IconItem<String>? = null
        private set

    var buildConfiguration: IconItem<Unit>? = null
        private set

    var targetFramework: IconItem<Unit>? = null
        private set

    var noBuild = false
        private set

    private val migrationsProjects: Array<IconItem<String>>
    private val startupProjects: Array<IconItem<StartupProjectData>>
    private val dotnetProject: IconItem<String>

    @Suppress("MemberVisibilityCanBePrivate")
    protected val migrationsProjectChangedEvent: Event<IconItem<String>> = Event()

    @Suppress("MemberVisibilityCanBePrivate")
    protected val startupProjectChangedEvent: Event<IconItem<StartupProjectData>> = Event()

    private lateinit var noBuildCheckbox: JBCheckBox
    private lateinit var buildConfigurationModel: DefaultComboBoxModel<IconItem<Unit>>
    private lateinit var targetFrameworkModel: DefaultComboBoxModel<IconItem<Unit>>
    private lateinit var dbContextModel: DefaultComboBoxModel<IconItem<String>>

    private lateinit var dbContextBox: ComboBox<IconItem<String>>

    private var prevPreferredMigrationsProjectName: String? = null
    private var prevPreferredStartupProjectName: String? = null

    init {
        this.title = title

        migrationsProjects = model.getAvailableMigrationsProjects
            .sync(Unit)
            .map { it.toIconItem() }
            .toTypedArray()

        startupProjects = model.getAvailableStartupProjects
            .sync(Unit)
            .map { it.toIconItem() }
            .toTypedArray()

        dotnetProject = migrationsProjects.find { it.displayName == currentDotnetProjectName } ?: migrationsProjects.first()

        migrationsProjectChangedEvent += ::migrationsProjectChanged
        startupProjectChangedEvent += ::startupProjectChanged

        targetFrameworkModel = DefaultComboBoxModel<IconItem<Unit>>()
        dbContextModel = DefaultComboBoxModel<IconItem<String>>()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            primaryOptions(this)
            additionalOptions(this)
        }
    }

    override fun doOKAction() {
        super.doOKAction()

        val commonOptionsService = CommonOptionsStateService.getInstance(intellijProject)

        if (prevPreferredMigrationsProjectName != null && prevPreferredStartupProjectName != null)
            commonOptionsService.clearPreferredProjects(prevPreferredMigrationsProjectName!!, prevPreferredStartupProjectName!!)

        commonOptionsService.setPreferredProjectsPair(migrationsProject!!.displayName, startupProject!!.displayName)
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
            val migrationsProjectBox = iconComboBox(migrationsBoxModel, { migrationsProject }, ::migrationsProjectSetter)
            if (shouldHaveMigrationsInProject) {
                migrationsProjectBox
                    .withValidationOnInput(migrationsProjectValidation())
                    .withValidationOnApply(migrationsProjectValidation())
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun startupProjectRow(parent: LayoutBuilder): Row {
        val startupBoxModel = DefaultComboBoxModel(startupProjects)

        return parent.row("Startup project:") {
            iconComboBox(startupBoxModel, { startupProject }, ::startupProjectSetter)
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
        val currentConfiguration = intellijProject.solution.solutionProperties.activeConfigurationPlatform.value!!

        val availableConfigurations = intellijProject.solution.solutionProperties.configurationsAndPlatformsCollection.valueOrNull!!
            .map { IconItem(it.configuration, DotnetIconResolver.resolveForType(DotnetIconType.BUILD_CONFIGURATION), Unit) }
            .toTypedArray()

        buildConfigurationModel = DefaultComboBoxModel(availableConfigurations)
        buildConfiguration = availableConfigurations.find { it.displayName == currentConfiguration.configuration }
            ?: availableConfigurations.first()

        return parent.row("Build configuration:") {
            iconComboBox(buildConfigurationModel, { buildConfiguration }, ::buildConfigurationSetter)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun targetFrameworkRow(parent: Row): Row {

        return parent.row("Target framework:") {
            iconComboBox(targetFrameworkModel, { targetFramework }, ::targetFrameworkSetter)
        }
    }

    private fun loadPreferredProjects() {
        val preferredProjects = CommonOptionsStateService.getInstance(intellijProject).getPreferredProjectPair(dotnetProject.displayName)
        if (preferredProjects != null) {
            val (migrationsProjectName, startupProjectName) = preferredProjects
            prevPreferredMigrationsProjectName = migrationsProjectName
            prevPreferredStartupProjectName = startupProjectName
            val migrationsProject = migrationsProjects.find { it.displayName == migrationsProjectName } ?: migrationsProjects.first()
            val startupProject = startupProjects.find { it.displayName == startupProjectName } ?: startupProjects.first()

            migrationsProjectSetter(migrationsProject)
            startupProjectSetter(startupProject)
        } else {
            migrationsProjectSetter(migrationsProjects.find { it.displayName == dotnetProject.displayName } ?: migrationsProjects.first())
            startupProjectSetter(startupProjects.find { it.displayName == dotnetProject.displayName } ?: startupProjects.first())
        }
    }

    private fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<IconItem<String>>) -> ValidationInfo? = {
        if (migrationsProject == null)
            null
        else {
            val hasMigrations = model.hasAvailableMigrations.runUnderProgress(migrationsProject!!.displayName, intellijProject, "Checking migrations...",
                isCancelable = true,
                throwFault = true
            )

            if (hasMigrations == null || !hasMigrations)
                error("Selected migrations project doesn't have migrations")
            else
                null
        }
    }

    private fun dbContextValidation(): ValidationInfoBuilder.(ComboBox<IconItem<String>>) -> ValidationInfo? = {
        if (dbContext == null || dbContextModel.size == 0)
            error("Migrations project should have at least 1 DbContext")
        else
            null
    }

    private fun migrationsProjectSetter(project: IconItem<String>?) {
        if (project == migrationsProject) return

        migrationsProject = project
        migrationsProjectChangedEvent.invoke(migrationsProject!!)
    }

    private fun startupProjectSetter(project: IconItem<StartupProjectData>?) {
        if (project == startupProject) return

        startupProject = project
        startupProjectChangedEvent.invoke(startupProject!!)
    }

    private fun dbContextSetter(context: IconItem<String>?) {
        if (context == dbContext) return

        dbContext = context
    }

    private fun buildConfigurationSetter(configuration: IconItem<Unit>?) {
        if (configuration == buildConfiguration) return

        buildConfiguration = configuration
    }

    private fun targetFrameworkSetter(framework: IconItem<Unit>?) {
        if (framework == targetFramework) return

        targetFramework = framework
    }

    private fun migrationsProjectChanged(project: IconItem<String>?) {
        dbContextModel.removeAllElements()

        if (project == null) return

        val dbContexts = model.getAvailableDbContexts.runUnderProgress(migrationsProject!!.displayName, intellijProject, "Loading DbContext classes...",
            isCancelable = true,
            throwFault = true
        )

        val dbContextIconItems = dbContexts!!.map { IconItem(it.name, DotnetIconResolver.resolveForType(DotnetIconType.CLASS), it.fullName) }

        dbContextModel.addAll(dbContextIconItems)

        dbContext = dbContextIconItems.firstOrNull()

        if (::dbContextBox.isInitialized)
            dbContextBox.item = dbContext
    }

    private fun startupProjectChanged(project: IconItem<StartupProjectData>?) {
        targetFrameworkModel.removeAllElements()

        if (project == null) return

        val configurationIconItems = project.data.targetFrameworks
            .map { IconItem(it, DotnetIconResolver.resolveForType(DotnetIconType.TARGET_FRAMEWORK), Unit) }

        targetFrameworkModel.addAll(configurationIconItems)

        // TODO: Handle if there is 0 build configurations
        targetFramework = configurationIconItems.first()
    }
}