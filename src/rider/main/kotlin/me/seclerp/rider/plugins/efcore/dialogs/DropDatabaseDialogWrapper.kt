package me.seclerp.rider.plugins.efcore.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.showYesNoDialog
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class DropDatabaseDialogWrapper(
    model: RiderEfCoreModel,
    private val intellijProject: Project,
    currentDotnetProjectName: String,
) : BaseEfCoreDialogWrapper("Drop Database", model, intellijProject, currentDotnetProjectName, false) {

    init {
        init()
    }

    override fun doOKAction() {
        if (showYesNoDialog(
                "Confirmation",
             "Are you sure that you want to drop database, used by ${dbContext!!.displayName}? This action can't be undone.",
                     intellijProject)) {
            super.doOKAction()
        }
    }
}