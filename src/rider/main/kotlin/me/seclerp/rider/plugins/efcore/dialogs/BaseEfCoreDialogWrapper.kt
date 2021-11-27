package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import com.jetbrains.rd.ide.model.ProjectInfo
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.projectComboBox
import javax.swing.DefaultComboBoxModel

abstract class BaseEfCoreDialogWrapper(
    title: String,
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun primaryOptions(parent: LayoutBuilder, customOptions: LayoutBuilder.() -> Unit) {
        customOptions(parent)
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
        migrationsProjectSetter(currentProject)

        return parent.row("Migrations project:") {
            projectComboBox(migrationsBoxModel, { migrationsProject }, ::migrationsProjectSetter)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun startupProjectRow(parent: LayoutBuilder): Row {
        val startupBoxModel = DefaultComboBoxModel(startupProjects)
        startupProjectSetter(currentProject)

        return parent.row("Startup project:") {
            projectComboBox(startupBoxModel, { startupProject }, ::startupProjectSetter)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun noBuildRow(parent: Row) =
        parent.row {
            checkBox("Skip project build process (--no-build)", { noBuild }, { noBuild = it })
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