package me.seclerp.rider.plugins.efcore.features.migrations.remove

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.rider.plugins.efcore.cli.api.MigrationsCommandFactory
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommand
import me.seclerp.rider.plugins.efcore.cli.execution.CliCommandResult
import me.seclerp.rider.plugins.efcore.features.shared.BaseDialogWrapper
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.ui.items.DbContextItem
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem

class RemoveLastMigrationDialogWrapper(
    intellijProject: Project,
    selectedDotnetProjectName: String?,
) : BaseDialogWrapper("Remove Last Migration", intellijProject, selectedDotnetProjectName, true) {
    val migrationsCommandFactory = intellijProject.service<MigrationsCommandFactory>()

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

    override fun generateCommand(): CliCommand {
        val commonOptions = getCommonOptions()

        return migrationsCommandFactory.removeLast(commonOptions)
    }

    override fun postCommandExecute(commandResult: CliCommandResult) {
        if (!commandResult.succeeded) {
            return
        }

        val folderService = intellijProject.service<RemoveLastMigrationFolderService>()
        folderService.deleteMigrationsFolderIfEmpty(availableMigrationsList.first())
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

        availableMigrationsList = beModel.getAvailableMigrations.runUnderProgress(
            migrationsIdentity,
            intellijProject,
            "Loading migrations...",
            isCancelable = true,
            throwFault = true
        )?.sortedByDescending { it.migrationLongName } ?: listOf()
    }
}