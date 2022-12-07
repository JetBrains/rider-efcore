package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class TryCommandAgainAction(
    private val retryAction: () -> Unit
): AnAction("Try Again") {
    override fun actionPerformed(p0: AnActionEvent) {
        retryAction()
    }
}