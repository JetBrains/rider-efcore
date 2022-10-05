package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.model.RdUnloadProjectDescriptor
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities

fun AnActionEvent.isEfCoreActionContext(): Boolean {
    if (project == null) return false

    // Case when we are presenting action from Tools application menu entry (always visible)
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE)
    if (!ActionPlaces.isPopupPlace(place) || actionFile == null) {
        return true
    }

    if (!isSupportedProjectExtension(actionFile.extension ?: "")) {
        return false
    }

    getFileProject(project!!, actionFile) ?: return false

    return true
}

fun AnActionEvent.getDotnetProjectName(): String? {
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return ""

    return getFileProject(project!!, actionFile)?.descriptor?.name
}

@Suppress("UnstableApiUsage")
private fun getFileProject(intellijProject: Project, virtualFile: VirtualFile): ProjectModelEntity? =
    WorkspaceModel
        .getInstance(intellijProject)
        .getProjectModelEntities(virtualFile, intellijProject)
        .firstOrNull {
            it.descriptor is RdProjectDescriptor
            && it.descriptor !is RdUnloadProjectDescriptor
        }

private fun isSupportedProjectExtension(projectFileExtension: String) =
    projectFileExtension == "csproj"
    || projectFileExtension == "fsproj"