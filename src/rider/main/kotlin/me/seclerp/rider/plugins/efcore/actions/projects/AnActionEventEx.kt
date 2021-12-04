package me.seclerp.rider.plugins.efcore.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.containingEntity
import com.jetbrains.rider.projectView.workspace.isUnloadedProject

fun AnActionEvent.isLoadedProjectFile(): Boolean {
    if (project == null) return false
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return false
    val dotnetProject = actionFile.containingEntity(project!!) ?: return false

    return !dotnetProject.isUnloadedProject()
        && (actionFile.extension.equals("csproj") || actionFile.extension.equals("fsproj"))
}

fun AnActionEvent.getDotnetProjectName(): String = getDotnetProject().name

fun AnActionEvent.getDotnetProject(): ProjectModelEntity {
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE)!!
    val projectModelEntity = actionFile.containingEntity(project!!)
    return projectModelEntity!!
}