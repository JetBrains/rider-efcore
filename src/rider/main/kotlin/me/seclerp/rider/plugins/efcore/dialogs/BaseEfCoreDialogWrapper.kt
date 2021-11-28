package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.jetbrains.rd.ide.model.ProjectInfo
import com.jetbrains.rd.ide.model.RiderEfCoreModel
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.projectComboBox
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import javax.swing.DefaultComboBoxModel

abstract class BaseEfCoreDialogWrapper(
    title: String,
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    private val currentDotnetProjectName: String,
    private val shouldHaveMigrationsInProject: Boolean = false
): DialogWrapper(true) {
    var migrationsProject: ProjectInfo? = null
        private set

    var startupProject: ProjectInfo? = null
        private set

    var noBuild = false
        private set

    private val migrationsProjects: Array<ProjectInfo> = model.getAvailableMigrationsProjects.sync(Unit).toTypedArray()
    private val startupProjects: Array<ProjectInfo> = model.getAvailableStartupProjects.sync(Unit).toTypedArray()
    private val dotnetProject = migrationsProjects.find { it.name == currentDotnetProjectName } ?: migrationsProjects.first()

    @Suppress("MemberVisibilityCanBePrivate")
    protected val migrationsProjectChangedEvent: Event<ProjectInfo> = Event()

    @Suppress("MemberVisibilityCanBePrivate")
    protected val startupProjectChangedEvent: Event<ProjectInfo> = Event()

    init {
        this.title = title
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            primaryOptions(this)
            additionalOptions(this)
        }
    }

    override fun doOKAction() {
        super.doOKAction()

        CommonOptionsStateService.getInstance(intellijProject).setPreferredProjectsPair(migrationsProject!!.name, startupProject!!.name)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun primaryOptions(parent: LayoutBuilder, customOptions: LayoutBuilder.() -> Unit) {
        customOptions(parent)
        loadPreferredProjects()
        migrationsProjectRow(parent)
        startupProjectRow(parent)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun primaryOptions(parent: LayoutBuilder) = primaryOptions(parent) { }

    @Suppress("MemberVisibilityCanBePrivate")
    fun additionalOptions(parent: LayoutBuilder, customOptions: Row.() -> Unit) {
        parent.titledRow("Additional Options") {
            customOptions(this)
            noBuildRow(this)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun additionalOptions(parent: LayoutBuilder) = additionalOptions(parent) { }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun migrationsProjectRow(parent: LayoutBuilder): Row {
        val migrationsBoxModel = DefaultComboBoxModel(migrationsProjects)

        return parent.row("Migrations project:") {
            val migrationsProjectBox = projectComboBox(migrationsBoxModel, { migrationsProject }, ::migrationsProjectSetter)
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
            projectComboBox(startupBoxModel, { startupProject }, ::startupProjectSetter)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun noBuildRow(parent: Row) =
        parent.row {
            checkBox("Skip project build process (--no-build)", { noBuild }, { noBuild = it })
        }

    private fun loadPreferredProjects() {
        val preferredProjects = CommonOptionsStateService.getInstance(intellijProject).getPreferredProjectPair(dotnetProject.name)
        if (preferredProjects != null) {
            val (migrationsProjectName, startupProjectName) = preferredProjects
            val migrationsProject = migrationsProjects.find { it.name == migrationsProjectName } ?: migrationsProjects.first()
            val startupProject = startupProjects.find { it.name == startupProjectName } ?: startupProjects.first()

            migrationsProjectSetter(migrationsProject)
            startupProjectSetter(startupProject)
        } else {
            migrationsProjectSetter(migrationsProjects.find { it.name == dotnetProject.name } ?: migrationsProjects.first())
            startupProjectSetter(startupProjects.find { it.name == dotnetProject.name } ?: startupProjects.first())
        }
    }

    private fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<ProjectInfo>) -> ValidationInfo? = {
        if (migrationsProject == null)
            null

        else {
            val hasMigrations = model.hasAvailableMigrations.runUnderProgress(migrationsProject!!.name, intellijProject, "Checking migrations...",
                isCancelable = true,
                throwFault = true
            )

            if (hasMigrations == null || !hasMigrations)
                error("Selected migrations project doesn't have migrations")
            else
                null
        }
    }

    private fun migrationsProjectSetter(project: ProjectInfo?) {
        if (project == migrationsProject) return

        migrationsProject = project
        migrationsProjectChangedEvent.invoke(migrationsProject!!)
    }

    private fun startupProjectSetter(project: ProjectInfo?) {
        if (project == startupProject) return

        startupProject = project
        startupProjectChangedEvent.invoke(startupProject!!)
    }
}