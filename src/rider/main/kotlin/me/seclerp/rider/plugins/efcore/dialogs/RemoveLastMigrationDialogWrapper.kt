package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.jetbrains.rd.ide.model.RiderEfCoreModel

class RemoveLastMigrationDialogWrapper(
    model: RiderEfCoreModel,
    intellijProject: Project,
    currentDotnetProjectName: String,
) : BaseEfCoreDialogWrapper("Remove Last Migration", model, intellijProject, currentDotnetProjectName, true) {

    init {
        init()
    }
}