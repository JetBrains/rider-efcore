package me.seclerp.rider.plugins.efcore.dialogs

class RemoveLastMigrationDialogWrapper(
    currentProjectName: String,
    migrationsProjects: Array<String>,
    startupProjects: Array<String>
) : EfCoreCommandDialogWrapper("Remove Last Migration", currentProjectName, migrationsProjects, startupProjects) {

    init {
        init()
    }
}