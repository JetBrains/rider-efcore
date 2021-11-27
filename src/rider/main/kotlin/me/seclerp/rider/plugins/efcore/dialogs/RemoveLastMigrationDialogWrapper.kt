package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.jetbrains.rd.ide.model.ProjectInfo

class RemoveLastMigrationDialogWrapper(
    intellijProject: Project,
    currentProject: ProjectInfo,
    migrationsProjects: Array<ProjectInfo>,
    startupProjects: Array<ProjectInfo>
) : BaseEfCoreDialogWrapper("Remove Last Migration", intellijProject, currentProject, migrationsProjects, startupProjects) {

    init {
        init()
    }
}