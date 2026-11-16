package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rd.util.threading.coroutines.launch
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import kotlinx.coroutines.Dispatchers

class TryCommandAgainAction(
    private val retryAction: suspend () -> Unit
): AnAction(EfCoreUiBundle.message("action.try.again.text")) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        project.lifetime.launch(Dispatchers.Default) {
            retryAction()
        }
    }
}