package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionPlaces.MAIN_TOOLBAR
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities
import com.jetbrains.rider.projectView.workspace.isProject
import org.jetbrains.annotations.NonNls
import java.util.UUID

fun AnActionEvent.isEfCoreActionContext() = when {
  isProjectsModeContext() -> true
  isSolutionModeContext() -> true
  else -> false
}

fun AnActionEvent.isProjectsModeContext(): Boolean {
  // Check if action is executing through Solution view, otherwise VIRTUAL_FILE from below may point to wrong file from currently opened editor.
  if (place != ActionPlaces.PROJECT_VIEW_POPUP) return false
  // Check if solution is loaded
  if (project == null) return false
  // Fast check if action file extension is supported (F# or C# project)
  val extension = getData(PlatformDataKeys.VIRTUAL_FILE)?.extension ?: return false
  if (!isSupportedProjectExtension(extension)) return false
  // Check that currently opened project is loaded and known by backend
  if (actionDotnetProjectFile == null) return false
  return true
}

fun AnActionEvent.isSolutionModeContext(): Boolean {
  val extension = getData(PlatformDataKeys.VIRTUAL_FILE)?.extension
  return when {
    ActionPlaces.isMainMenuOrActionSearch(place) -> true
    place == MAIN_TOOLBAR -> true
    isFromContextMenu && extension == "sln" || extension == "slnf" -> true
    else -> false
  }
}

val AnActionEvent.actionDotnetProjectFile: ProjectModelEntity?
  get() {
    if (place != ActionPlaces.PROJECT_VIEW_POPUP) return null
    val intellijProject = project ?: return null
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return null

    return WorkspaceModel
      .getInstance(intellijProject)
      .getProjectModelEntities(actionFile, intellijProject)
      .firstOrNull { it.isProject() && it.descriptor.let { it as RdProjectDescriptor }.isDotNetCore }
  }

val AnActionEvent.actionDotnetProjectId: UUID?
  get() =
    actionDotnetProjectFile?.descriptor?.let { it as? RdProjectDescriptor }?.originalGuid

@NonNls
private fun isSupportedProjectExtension(projectFileExtension: String) =
  projectFileExtension == "csproj"
  || projectFileExtension == "fsproj"