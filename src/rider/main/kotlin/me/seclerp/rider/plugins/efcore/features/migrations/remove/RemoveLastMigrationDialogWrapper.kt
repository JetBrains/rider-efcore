package me.seclerp.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class RemoveLastMigrationDialogWrapper(
    model: RiderEfCoreModel,
    intellijProject: Project,
    currentDotnetProjectName: String,
) : EfCoreDialogWrapper("Remove Last Migration", model, intellijProject, currentDotnetProjectName, true) {

    init {
        init()
    }
}