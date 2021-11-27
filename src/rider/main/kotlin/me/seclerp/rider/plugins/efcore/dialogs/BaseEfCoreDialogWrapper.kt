package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import com.jetbrains.rd.ide.model.ProjectInfo
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.projectComboBox
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import javax.swing.DefaultComboBoxModel

abstract class BaseEfCoreDialogWrapper(
    title: String,
    private val intellijProject: Project,
    private val currentProject: ProjectInfo,
    private val migrationsProjects: Array<ProjectInfo>,
    private val startupProjects: Array<ProjectInfo>
): DialogWrapper(true) {
    var migrationsProject: ProjectInfo? = null
        private set

    var startupProject: ProjectInfo? = null
        private set

    var noBuild = false
        private set

    protected val migrationsProjectNameChangedEvent: Event<ProjectInfo> = Event()

    protected val startupProjectNameChangedEvent: Event<ProjectInfo> = Event()

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
            projectComboBox(migrationsBoxModel, { migrationsProject }, ::migrationsProjectSetter)
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
        val preferredProjects = CommonOptionsStateService.getInstance(intellijProject).getPreferredProjectPair(currentProject.name)
        if (preferredProjects != null) {
            val (migrationsProjectName, startupProjectName) = preferredProjects
            val migrationsProject = migrationsProjects.find { it.name == migrationsProjectName } ?: migrationsProjects.first()
            val startupProject = startupProjects.find { it.name == startupProjectName } ?: startupProjects.first()

            migrationsProjectSetter(migrationsProject)
            startupProjectSetter(startupProject)
        } else {
            migrationsProjectSetter(migrationsProjects.find { it.name == currentProject.name } ?: migrationsProjects.first())
            startupProjectSetter(startupProjects.find { it.name == currentProject.name } ?: startupProjects.first())
        }
    }

    private fun migrationsProjectSetter(project: ProjectInfo?) {
        if (project == migrationsProject) return

        migrationsProject = project
        migrationsProjectNameChangedEvent.invoke(migrationsProject!!)
    }

    private fun startupProjectSetter(project: ProjectInfo?) {
        if (project == startupProject) return

        startupProject = project
        startupProjectNameChangedEvent.invoke(startupProject!!)
    }
}