package com.jetbrains.rider.plugins.efcore.cli.execution

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import icons.RiderIcons

@Service(Service.Level.PROJECT)
class EfCoreConsoleToolWindowProvider(intellijProject: Project) {
    companion object {
        private val TOOL_WINDOW_TASK_ID = EfCoreUiBundle.message("tab.task.ef.core")

        fun getInstance(project: Project) = project.service<EfCoreConsoleToolWindowProvider>()
    }

    private val toolWindow by lazy {
        ToolWindowManager.getInstance(intellijProject).registerToolWindow(TOOL_WINDOW_TASK_ID) {
            anchor = ToolWindowAnchor.BOTTOM
            canCloseContent = true
            icon = RiderIcons.Toolwindows.ToolwindowEFCore
        }
    }

    fun createTab(command: CliCommand, console: ConsoleView) {
        val contentManager = toolWindow.contentManager
        val factory = contentManager.factory
        val content = factory.createContent(console.component, command.presentationInfo.name, true)
        content.setDisposer(console)
        contentManager.addContent(content)
        toolWindow.activate {
            contentManager.setSelectedContent(content)
        }
    }
}