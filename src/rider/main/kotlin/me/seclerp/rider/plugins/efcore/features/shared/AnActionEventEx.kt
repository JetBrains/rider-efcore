package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.model.RdUnloadProjectDescriptor
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.getProjectModelEntities
import org.jetbrains.annotations.NonNls
import java.util.*

fun AnActionEvent.isEfCoreActionContext(): Boolean {
    // Solution not loaded, hide action
    if (project == null) return false

    // Tools section
    if (place == ActionPlaces.MAIN_MENU) return true

    // Search
    if (place == ActionPlaces.ACTION_SEARCH) return true

    // Other potential popups, except project view popup (project context menu)
    if (ActionPlaces.isPopupPlace(place) && place != ActionPlaces.PROJECT_VIEW_POPUP) return true

    // If we're trying to show action from context menu, but not under project context menu, hide
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return false
    if (!isSupportedProjectExtension(actionFile.extension ?: ""))
        return false

    // Lastly we check that the supported project file is loaded by MSBuild and has correct descriptor
    getFileProject(project!!, actionFile) ?: return false

    return true
}

fun AnActionEvent.getDotnetProjectId(): UUID? {
    val actionFile = getData(PlatformDataKeys.VIRTUAL_FILE) ?: return null

    return getFileProject(project!!, actionFile)?.descriptor?.let {
        (it as RdProjectDescriptor).originalGuid
    }
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

@NonNls
private fun isSupportedProjectExtension(projectFileExtension: String) =
    projectFileExtension == "csproj"
    || projectFileExtension == "fsproj"