package me.seclerp.rider.plugins.efcore.features.database.update

import com.intellij.openapi.project.Project
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.observables.ObservableProperty
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class UpdateDatabaseDataContext(
    intellijProject: Project,
    commonCtx: CommonDataContext,
    beModel: RiderEfCoreModel
) {
    var availableMigrations = ObservableProperty(listOf<MigrationInfo>())

    var targetMigration = ObservableProperty("")
    var useDefaultConnection = ObservableProperty(true)
    var connection = ObservableProperty("")

    init {
        commonCtx.dbContext.afterChange(true) {
            val migrationProjectName = commonCtx.migrationsProject.notNullValue.name
            val dbContextName = it!!.fullName
            val migrations = beModel.getAvailableMigrations.runUnderProgress(
                MigrationsIdentity(migrationProjectName, dbContextName), intellijProject, "Loading available migrations...",
                isCancelable = true,
                throwFault = true
            )

            if (migrations != null) {
                availableMigrations.value = migrations
            }
        }

        availableMigrations.afterChange {
            if (it!!.isEmpty()) {
                targetMigration.value = ""
            } else {
                targetMigration.value = it.first().migrationLongName
            }
        }
    }
}