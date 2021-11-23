package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.panel
import me.seclerp.rider.plugins.efcore.components.ProjectInfoComboBoxRendererAdapter
import javax.swing.DefaultComboBoxModel

abstract class EfCoreCommandDialogWrapper(
    title: String,
    currentProjectName: String,
    private val migrationsProjects: Array<String>,
    private val startupProjects: Array<String>
): DialogWrapper(true) {
    var migrationsProjectName = currentProjectName
        private set

    var startupProjectName = currentProjectName
        private set

    var noBuild = false
        private set

    private val migrationsBoxModel = DefaultComboBoxModel(migrationsProjects)
    private val startupBoxModel = DefaultComboBoxModel(startupProjects)

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
    protected fun migrationsProjectRow(it: LayoutBuilder) =
        it.row("Migrations project:") {
            cell(isFullWidth = true) {
                comboBox(migrationsBoxModel, ::migrationsProjectName, ProjectInfoComboBoxRendererAdapter())
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun startupProjectRow(it: LayoutBuilder) =
        it.row("Startup project:") {
            cell(isFullWidth = true) {
                comboBox(startupBoxModel, ::startupProjectName, ProjectInfoComboBoxRendererAdapter())
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun noBuildRow(it: LayoutBuilder) =
        it.row {
            checkBox("Skip project build process (--no-build)", ::noBuild)
        }
}