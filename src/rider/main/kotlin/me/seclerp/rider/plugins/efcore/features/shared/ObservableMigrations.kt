package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.observables.ObservableCollection
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bind
import me.seclerp.rider.plugins.efcore.rd.*

class ObservableMigrations(
    private val intellijProject: Project,
    private val migrationsProject: ObservableProperty<MigrationsProjectInfo?>,
    private val dbContext: ObservableProperty<DbContextInfo?>
): ObservableCollection<MigrationInfo>() {
    fun initBinding() {
        this.bind(dbContext) {
            if (it != null) {
                val migrationsProjectId = migrationsProject.value!!.id
                val dbContextName = it.fullName
                val migrations = intellijProject.solution.riderEfCoreModel.getAvailableMigrations.runUnderProgress(
                    MigrationsIdentity(migrationsProjectId, dbContextName), intellijProject, "Loading available migrations...",
                    isCancelable = true,
                    throwFault = true
                )

                migrations ?: listOf()
            } else {
                listOf()
            }
        }
    }
}