package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory

class OpenQuickEfCoreActionsAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "EF Core Quick Actions",
                QuickActionsGroup(),
                actionEvent.dataContext,
                JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING,
                false
            )

        if (actionEvent.project != null) {
            popup.showCenteredInCurrentWindow(actionEvent.project!!)
        } else {
            popup.showInFocusCenter()
        }
    }
}