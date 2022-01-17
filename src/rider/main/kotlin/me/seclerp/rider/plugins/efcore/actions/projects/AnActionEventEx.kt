package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities
import com.jetbrains.rider.projectView.workspace.isUnloadedProject

fun AnActionEvent.isLoadedProjectFile(): Boolean {
    if (project == null) return false
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return false
    val dotnetProject = getDotnetProject() ?: return false

    return !dotnetProject.isUnloadedProject()
        && (actionFile.extension.equals("csproj") || actionFile.extension.equals("fsproj"))
}

fun AnActionEvent.getDotnetProjectName(): String = getDotnetProject()!!.name

private fun AnActionEvent.getDotnetProject(): ProjectModelEntity? {
    return getDotnetProjects(project!!, getData(PlatformDataKeys.VIRTUAL_FILE)!!).firstOrNull()
}

@Suppress("UnstableApiUsage")
private fun getDotnetProjects(intellijProject: Project, virtualFile: VirtualFile): List<ProjectModelEntity> {
    return WorkspaceModel
        .getInstance(intellijProject)
        .getProjectModelEntities(virtualFile, intellijProject)
}