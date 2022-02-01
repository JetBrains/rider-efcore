package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionManagerEx

class QuickActionsGroup : ActionGroup() {
    override fun getChildren(actionEvent: AnActionEvent?): Array<AnAction> {
        val actions = mutableListOf<AnAction>()
        val actionManager = ActionManager.getInstance() as ActionManagerEx

        for (id in actionManager.getActionIdList("EfCore.Features.")) {
            actions.add(actionManager.getAction(id!!))
        }

        return actions.toTypedArray()
    }
}