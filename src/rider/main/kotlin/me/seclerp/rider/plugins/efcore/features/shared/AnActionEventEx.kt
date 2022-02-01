package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities
import com.jetbrains.rider.projectView.workspace.isUnloadedProject

fun AnActionEvent.isEfCoreActionContext(): Boolean {
    if (project == null) return false
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return true
    val dotnetProject = getDotnetProject() ?: return true

    return !dotnetProject.isUnloadedProject()
        && (actionFile.extension.equals("csproj") || actionFile.extension.equals("fsproj"))
}

fun AnActionEvent.getDotnetProjectName(): String? {
    return getDotnetProject()?.descriptor?.name
}

private fun AnActionEvent.getDotnetProject(): ProjectModelEntity? {
    val selectedFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return null

    return getDotnetProjects(project!!, selectedFile).firstOrNull {
        it.descriptor is RdProjectDescriptor
    }
}

@Suppress("UnstableApiUsage")
private fun getDotnetProjects(intellijProject: Project, virtualFile: VirtualFile): List<ProjectModelEntity> {
    return WorkspaceModel
        .getInstance(intellijProject)
        .getProjectModelEntities(virtualFile, intellijProject)
}