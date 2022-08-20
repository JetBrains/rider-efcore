package me.seclerp.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.items.*

class CommonDialogValidator(
    private val dataCtx: CommonDataContext,
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    private val shouldHaveMigrationsInProject: Boolean
) {
    fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<MigrationsProjectItem>) -> ValidationInfo? = {
        if (it.item == null)
            error("You should selected valid migrations project")
        else if (shouldHaveMigrationsInProject) {
            if (dataCtx.dbContext.value == null)
                null
            else {
                val migrationsIdentity = MigrationsIdentity(
                    it.item.displayName,
                    dataCtx.dbContext.value!!.fullName)

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
        if (it.item == null)
            error("You should selected valid startup project")
        else
            null
    }

    fun dbContextValidation(): ValidationInfoBuilder.(ComboBox<DbContextItem>) -> ValidationInfo? = {
        if (it.item == null || dataCtx.availableDbContexts.value.isEmpty())
            error("Migrations project should have at least 1 DbContext")
        else
            null
    }

    fun buildConfigurationValidation(): ValidationInfoBuilder.(ComboBox<BuildConfigurationItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.item == null || dataCtx.availableBuildConfigurations.isEmpty()))
            error("Solution doesn't have any build configurations")
        else
            null
    }

    fun targetFrameworkValidation(): ValidationInfoBuilder.(ComboBox<BaseTargetFrameworkItem>) -> ValidationInfo? = {
        if (it.isEnabled && (it.item == null || dataCtx.availableTargetFrameworks.value.isEmpty()))
            error("Startup project should have at least 1 supported target framework")
        else
            null
    }
}