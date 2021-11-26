package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import me.seclerp.rider.plugins.efcore.Event
import me.seclerp.rider.plugins.efcore.components.ProjectInfoComboBoxRendererAdapter
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel

abstract class EfCoreCommandDialogWrapper(
    title: String,
    private val currentProjectName: String,
    private val migrationsProjects: Array<String>,
    private val startupProjects: Array<String>
): DialogWrapper(true) {
    var migrationsProjectName: String? = null
        private set

    var startupProjectName: String? = null
        private set

    var noBuild = false
        private set

    protected val migrationsProjectNameChangedEvent: Event<String> = Event()
    protected val startupProjectNameChangedEvent: Event<String> = Event()

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
        migrationsProjectName = currentProjectName

        return it.row("Migrations project:") {
            cell(isFullWidth = true) {
                comboBox(migrationsBoxModel,
                    { migrationsProjectName },
                    ::migrationsProjectNameSetter,
                    ProjectInfoComboBoxRendererAdapter())
                    .constraints(CCFlags.pushX, CCFlags.growX)
                    // Setter provided above called only on submit, so we need additional change detection
                    .component.addItemListener {
                        if (it.stateChange == ItemEvent.SELECTED) {
                            migrationsProjectNameSetter(it.item as String)
                        }
                    }
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun startupProjectRow(it: LayoutBuilder): Row {
        val startupBoxModel = DefaultComboBoxModel(startupProjects)
        startupProjectName = currentProjectName

        return it.row("Startup project:") {
            cell(isFullWidth = true) {
                comboBox(startupBoxModel,
                    { startupProjectName },
                    ::startupProjectNameSetter,
                    ProjectInfoComboBoxRendererAdapter())
                    .constraints(CCFlags.pushX, CCFlags.growX)
                    // Setter provided above called only on submit, so we need additional change detection
                    .component.addItemListener {
                        if (it.stateChange == ItemEvent.SELECTED) {
                            startupProjectNameSetter(it.item as String)
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

    private fun migrationsProjectNameSetter(it: String?) {
        if (it == migrationsProjectName) return

        migrationsProjectName = it
        migrationsProjectNameChangedEvent.invoke(migrationsProjectName!!)
    }

    private fun startupProjectNameSetter(it: String?) {
        if (it == startupProjectName) return

        startupProjectName = it
        startupProjectNameChangedEvent.invoke(startupProjectName!!)
    }
}