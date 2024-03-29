package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import org.jetbrains.annotations.NonNls

class QuickActionsGroup : ActionGroup() {
    override fun getChildren(actionEvent: AnActionEvent?): Array<AnAction> {
        val actions = mutableListOf<AnAction>()
        val actionManager = ActionManager.getInstance() as ActionManagerEx

        @NonNls
        val actionIdPrefix = "EfCore.Features."
        for (id in actionManager.getActionIdList(actionIdPrefix)) {
            actions.add(actionManager.getAction(id!!))
        }

        return actions.toTypedArray()
    }
}