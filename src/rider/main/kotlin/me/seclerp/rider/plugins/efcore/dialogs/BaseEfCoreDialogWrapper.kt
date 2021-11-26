package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import com.jetbrains.rd.ide.model.ProjectInfo
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.ProjectInfoComboBoxRendererAdapter
import java.awt.event.ItemEvent
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
    fun primaryOptions(it: LayoutBuilder, customOptions: (LayoutBuilder) -> Unit) {
        customOptions(it)
        migrationsProjectRow(it)
        startupProjectRow(it)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun primaryOptions(it: LayoutBuilder) = primaryOptions(it) { }

    @Suppress("MemberVisibilityCanBePrivate")
    fun additionalOptions(it: LayoutBuilder, customOptions: (LayoutBuilder) -> Unit) {
        it.titledRow("Additional") {
            customOptions(it)
            noBuildRow(it)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun additionalOptions(it: LayoutBuilder) = additionalOptions(it) { }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun migrationsProjectRow(it: LayoutBuilder): Row {
        val migrationsBoxModel = DefaultComboBoxModel(migrationsProjects)
        migrationsProject = currentProject

        return it.row("Migrations project:") {
            cell(isFullWidth = true) {
                comboBox(migrationsBoxModel,
                    { migrationsProject },
                    ::migrationsProjectNameSetter,
                    ProjectInfoComboBoxRendererAdapter())
                    .constraints(CCFlags.pushX, CCFlags.growX)
                    // Setter provided above called only on submit, so we need additional change detection
                    .component.addItemListener {
                        if (it.stateChange == ItemEvent.SELECTED) {
                            migrationsProjectNameSetter(it.item as ProjectInfo)
                        }
                    }
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun startupProjectRow(it: LayoutBuilder): Row {
        val startupBoxModel = DefaultComboBoxModel(startupProjects)
        startupProject = currentProject

        return it.row("Startup project:") {
            cell(isFullWidth = true) {
                comboBox(startupBoxModel,
                    { startupProject },
                    ::startupProjectNameSetter,
                    ProjectInfoComboBoxRendererAdapter())
                    .constraints(CCFlags.pushX, CCFlags.growX)
                    // Setter provided above called only on submit, so we need additional change detection
                    .component.addItemListener {
                        if (it.stateChange == ItemEvent.SELECTED) {
                            startupProjectNameSetter(it.item as ProjectInfo)
                        }
                    }
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun noBuildRow(it: LayoutBuilder) =
        it.row {
            checkBox("Skip project build process (--no-build)", { noBuild }, { noBuild = it })
        }

    private fun migrationsProjectNameSetter(it: ProjectInfo?) {
        if (it == migrationsProject) return

        migrationsProject = it
        migrationsProjectNameChangedEvent.invoke(migrationsProject!!)
    }

    private fun startupProjectNameSetter(it: ProjectInfo?) {
        if (it == startupProject) return

        startupProject = it
        startupProjectNameChangedEvent.invoke(startupProject!!)
    }
}