package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle

class TryCommandAgainAction(
    private val retryAction: () -> Unit
): AnAction(EfCoreUiBundle.message("action.try.again.text")) {
    override fun actionPerformed(p0: AnActionEvent) {
        retryAction()
    }
}