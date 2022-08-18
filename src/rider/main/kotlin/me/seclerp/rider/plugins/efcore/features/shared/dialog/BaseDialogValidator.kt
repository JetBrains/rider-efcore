package me.seclerp.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.items.*

class BaseDialogValidator(
    private val dataCtx: CommonDataContext,
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    private val shouldHaveMigrationsInProject: Boolean
) {
    fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<MigrationsProjectItem>) -> ValidationInfo? = {
        if (dataCtx.migrationsProject.value == null)
            error("You should selected valid migrations project")
        else if (shouldHaveMigrationsInProject) {
            if (dataCtx.dbContext.value == null)
                null
            else {
                val migrationsIdentity = MigrationsIdentity(
                    dataCtx.migrationsProject.notNullValue.name,
                    dataCtx.dbContext.notNullValue.fullName)

                val hasMigrations = beModel.hasAvailableMigrations.runUnderProgress(
                    migrationsIdentity, intellijProject, "Checking migrations...",
                    isCancelable = true,
                    throwFault = true
                )

                if (hasMigrations == null || !hasMigrations)
                    error("Selected migrations project doesn't have migrations")
                else
                    null
            }
        } else null
    }

    fun startupProjectValidation(): ValidationInfoBuilder.(ComboBox<StartupProjectItem>) -> ValidationInfo? = {
        if (dataCtx.startupProject.value == null)
            error("You should selected valid startup project")
        else
            null
    }

    fun dbContextValidation(): ValidationInfoBuilder.(ComboBox<DbContextItem>) -> ValidationInfo? = {
        if (dataCtx.dbContext.value == null || dataCtx.availableDbContexts.notNullValue.isEmpty())
            error("Migrations project should have at least 1 DbContext")
        else
            null
    }

    fun buildConfigurationValidation(): ValidationInfoBuilder.(ComboBox<BuildConfigurationItem>) -> ValidationInfo? = {
        if (it.isEnabled && (dataCtx.buildConfiguration.value == null || dataCtx.availableBuildConfigurations.isEmpty()))
            error("Solution doesn't have any build configurations")
        else
            null
    }

    fun targetFrameworkValidation(): ValidationInfoBuilder.(ComboBox<BaseTargetFrameworkItem>) -> ValidationInfo? = {
        if (it.isEnabled && (dataCtx.targetFramework.value == null || dataCtx.availableTargetFrameworks.notNullValue.isEmpty()))
            error("Startup project should have at least 1 supported target framework")
        else
            null
    }
}