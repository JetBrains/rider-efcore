package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DefaultActionGroup

class EfCoreActionsGroup : DefaultActionGroup() {
  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  init {
    templatePresentation.isHideGroupIfEmpty = true
  }
}