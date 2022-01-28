package me.seclerp.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.project.Project
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.features.shared.EfCoreDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem

class RemoveLastMigrationDialogWrapper(
    private val model: RiderEfCoreModel,
    private val intellijProject: Project,
    currentDotnetProjectName: String,
) : EfCoreDialogWrapper("Remove Last Migration", model, intellijProject, currentDotnetProjectName, true) {

    var availableMigrationsList = listOf<MigrationInfo>()

    //
    // Constructor
    init {
        addMigrationsProjectChangedListener(::onMigrationsProjectChanged)
        addDbContextChangedListener(::onDbContextChanged)

        init()
    }

    private fun onMigrationsProjectChanged(migrationsProjectItem: MigrationsProjectItem) {
        refreshCurrentDbContextMigrations(commonOptions.dbContext)
    }

    private fun onDbContextChanged(dbContext: DbContextItem?) {
        refreshCurrentDbContextMigrations(dbContext)
    }

    //
    // Methods
    private fun refreshCurrentDbContextMigrations(dbContext: DbContextItem?) {
        val migrationsProjectName = commonOptions.migrationsProject!!.displayName
        val dbContextFullName = dbContext!!.data

        val migrationsIdentity = MigrationsIdentity(migrationsProjectName, dbContextFullName)

        loadMigrationsByContextName(migrationsIdentity)
    }

    private fun loadMigrationsByContextName(migrationsIdentity: MigrationsIdentity) {
        if (migrationsIdentity.dbContextClassFullName.isEmpty()) {
            availableMigrationsList = listOf()
            return
        }

        availableMigrationsList = model.getAvailableMigrations.runUnderProgress(
            migrationsIdentity,
            intellijProject,
            "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )?.sortedByDescending { it.migrationLongName } ?: listOf()
    }
}