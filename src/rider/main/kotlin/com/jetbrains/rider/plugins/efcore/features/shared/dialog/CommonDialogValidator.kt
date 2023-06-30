package com.jetbrains.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.util.idea.runUnderProgress
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.rd.MigrationsIdentity
import com.jetbrains.rider.plugins.efcore.rd.RiderEfCoreModel
import com.jetbrains.rider.plugins.efcore.ui.items.*

class CommonDialogValidator(
    private val dataCtx: CommonDataContext,
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    private val shouldHaveMigrationsForDbContext: Boolean
) {
    fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<MigrationsProjectItem>) -> ValidationInfo? = {
        if (it.item == null)
            error(EfCoreUiBundle.message("dialog.message.you.should.selected.valid.migrations.project"))
        else null
    }

    fun startupProjectValidation(): ValidationInfoBuilder.(ComboBox<StartupProjectItem>) -> ValidationInfo? = {
        if (it.item == null)
            error(EfCoreUiBundle.message("dialog.message.you.should.selected.valid.startup.project"))
        else
            null
    }

    fun dbContextValidation(): ValidationInfoBuilder.(ComboBox<DbContextItem>) -> ValidationInfo? = {
        if (it.item == null || dataCtx.availableDbContexts.value.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.migrations.project.should.have.at.least.dbcontext"))
        else if (shouldHaveMigrationsForDbContext) {
            if (dataCtx.dbContext.value == null || dataCtx.migrationsProject.value == null)
                null
            else {
                val migrationsIdentity = MigrationsIdentity(
                    dataCtx.migrationsProject.value!!.id,
                    it.item.data.fullName)

                val hasMigrations = beModel.hasAvailableMigrations.runUnderProgress(
                    migrationsIdentity, intellijProject, EfCoreUiBundle.message("progress.title.checking.migrations"),
                    isCancelable = true,
                    throwFault = true
                )

                if (hasMigrations == null || !hasMigrations)
                    error(EfCoreUiBundle.message("dialog.message.selected.dbcontext.doesnt.have.migrations"))
                else null
            }
        } else null
    }

    fun buildConfigurationValidation(): ValidationInfoBuilder.(ComboBox<BuildConfigurationItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.item == null || dataCtx.availableBuildConfigurations.value.isEmpty()))
            error(EfCoreUiBundle.message("dialog.message.solution.doesnt.have.any.build.configurations"))
        else
            null
    }

    fun targetFrameworkValidation(): ValidationInfoBuilder.(ComboBox<BaseTargetFrameworkItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.item == null || dataCtx.availableTargetFrameworks.value.isEmpty()))
            error(EfCoreUiBundle.message("dialog.message.startup.project.should.have.at.least.supported.target.framework"))
        else
            null
    }
}