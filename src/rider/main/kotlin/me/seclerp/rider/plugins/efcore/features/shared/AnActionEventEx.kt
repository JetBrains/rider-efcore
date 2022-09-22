package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities
import com.jetbrains.rider.projectView.workspace.isUnloadedProject

fun AnActionEvent.isEfCoreActionContext(): Boolean {
    if (project == null) return false
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return true

    if (!isSupportedProjectExtension(actionFile.extension ?: "")) {
        return false
    }

    val fileProject = getFileProject(project!!, actionFile) ?: return true

    return !fileProject.isUnloadedProject()
}

fun AnActionEvent.getDotnetProjectName(): String {
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return ""

    return getFileProject(project!!, actionFile)!!.descriptor.name
}

@Suppress("UnstableApiUsage")
private fun getFileProject(intellijProject: Project, virtualFile: VirtualFile): ProjectModelEntity? =
    WorkspaceModel
        .getInstance(intellijProject)
        .getProjectModelEntities(virtualFile, intellijProject)
        .firstOrNull()

private fun isSupportedProjectExtension(projectFileExtension: String) =
    projectFileExtension == "csproj"
    || projectFileExtension == "fsproj"