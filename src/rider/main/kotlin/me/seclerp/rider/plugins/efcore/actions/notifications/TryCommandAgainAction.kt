package me.seclerp.rider.plugins.efcore.actions.notifications

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.commands.CliCommandResult
import me.seclerp.rider.plugins.efcore.commands.executeCommandUnderProgress

class TryCommandAgainAction(
    private val project: Project,
    private val taskTitle: String,
    private val succeedText: String,
    private val shouldRefreshSolution: Boolean = true,
    private val what: (Unit) -> CliCommandResult
): AnAction("Try Again") {
    override fun actionPerformed(p0: AnActionEvent) {
        executeCommandUnderProgress(project, taskTitle, succeedText, shouldRefreshSolution, what)
    }
}