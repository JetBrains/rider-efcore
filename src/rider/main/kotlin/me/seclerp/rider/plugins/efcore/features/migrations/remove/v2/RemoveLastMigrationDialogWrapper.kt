package me.seclerp.rider.plugins.efcore.features.migrations.remove.v2

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.features.shared.v2.EfCoreDialogWrapper
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