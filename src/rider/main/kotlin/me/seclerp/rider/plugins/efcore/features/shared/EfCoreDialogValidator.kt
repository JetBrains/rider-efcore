package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.items.*
import javax.swing.DefaultComboBoxModel

class EfCoreDialogValidator(
    private val commonOptions: CommonOptionsModel,
    private val beModel: RiderEfCoreModel,
    private val intellijProject: Project,
    private val shouldHaveMigrationsInProject: Boolean,
    private val dbContextModel: DefaultComboBoxModel<DbContextItem>,
    private val availableBuildConfigurations: Array<BuildConfigurationItem>,
    private val targetFrameworkModel: DefaultComboBoxModel<BaseTargetFrameworkItem>
) {
    fun migrationsProjectValidation(): ValidationInfoBuilder.(ComboBox<MigrationsProjectItem>) -> ValidationInfo? = {
        if (commonOptions.migrationsProject == null)
            error("You should selected valid migrations project")
        else if (shouldHaveMigrationsInProject) {
            if (commonOptions.dbContext == null)
                null
            else {
                val migrationsIdentity = MigrationsIdentity(
                    commonOptions.migrationsProject!!.displayName,
                    commonOptions.dbContext!!.data)

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
        if (commonOptions.startupProject == null)
            error("You should selected valid startup project")
        else
            null
    }

    fun dbContextValidation(): ValidationInfoBuilder.(ComboBox<DbContextItem>) -> ValidationInfo? = {
        if (commonOptions.dbContext == null || dbContextModel.size == 0)
            error("Migrations project should have at least 1 DbContext")
        else
            null
    }

    fun buildConfigurationValidation(): ValidationInfoBuilder.(ComboBox<BuildConfigurationItem>) -> ValidationInfo? = {
        if (commonOptions.buildConfiguration == null || availableBuildConfigurations.isEmpty())
            error("Solution doesn't have any build configurations")
        else
            null
    }

    fun targetFrameworkValidation(): ValidationInfoBuilder.(ComboBox<BaseTargetFrameworkItem>) -> ValidationInfo? = {
        if (commonOptions.targetFramework == null || targetFrameworkModel.size == 0)
            error("Startup project should have at least 1 supported target framework")
        else
            null
    }
}