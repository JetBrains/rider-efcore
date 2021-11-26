package me.seclerp.rider.plugins.efcore.dialogs

import com.jetbrains.rd.ide.model.ProjectInfo

class RemoveLastMigrationDialogWrapper(
    currentProject: ProjectInfo,
    migrationsProjects: Array<ProjectInfo>,
    startupProjects: Array<ProjectInfo>
) : BaseEfCoreDialogWrapper("Remove Last Migration", currentProject, migrationsProjects, startupProjects) {

    init {
        init()
    }
}