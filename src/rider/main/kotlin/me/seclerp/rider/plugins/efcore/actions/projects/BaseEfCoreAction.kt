package me.seclerp.rider.plugins.efcore.actions.projects

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import me.seclerp.rider.plugins.efcore.actions.getDotnetProjectName
import me.seclerp.rider.plugins.efcore.actions.isLoadedProjectFile
import me.seclerp.rider.plugins.efcore.dialogs.BaseEfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.commands.CommonOptions

abstract class BaseEfCoreAction: AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isVisible = actionEvent.isLoadedProjectFile()
    }

    fun getCommonOptions(dialog: BaseEfCoreDialogWrapper): CommonOptions =
        CommonOptions(
            dialog.migrationsProject!!.data.fullPath,
            dialog.startupProject!!.data.fullPath,
            dialog.dbContext!!.data,
            dialog.buildConfiguration!!.displayName,
            dialog.targetFramework!!.displayName,
            dialog.noBuild
        )

    protected fun getEfCoreRiderModel(actionEvent: AnActionEvent): RiderEfCoreModel {
        // TODO: Validate

        return actionEvent.project?.solution?.riderEfCoreModel!!
    }

    protected fun <R> buildDialogInstance(actionEvent: AnActionEvent, dialogFactory: DialogBuildParameters.() -> R): R {
        val model = getEfCoreRiderModel(actionEvent)
        val currentDotnetProjectName = actionEvent.getDotnetProjectName()
        // TODO: Handle case when there is no appropriate projects
        val params = DialogBuildParameters(model, currentDotnetProjectName)

        return dialogFactory(params)
    }

    @Suppress("ArrayInDataClass")
    data class DialogBuildParameters(
        val model: RiderEfCoreModel,
        val currentDotnetProjectName: String)
}