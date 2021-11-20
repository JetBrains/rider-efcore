package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

abstract class EFCoreAction: AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isProjectFile()
    }
}