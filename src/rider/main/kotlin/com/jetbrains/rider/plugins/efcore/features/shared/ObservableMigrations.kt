package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.jetbrains.observables.ObservableProperty
import com.jetbrains.observables.bind
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.rd.DbContextInfo
import com.jetbrains.rider.plugins.efcore.rd.MigrationInfo
import com.jetbrains.rider.plugins.efcore.rd.MigrationsIdentity
import com.jetbrains.rider.plugins.efcore.rd.MigrationsProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress

class ObservableMigrations(
    private val intellijProject: Project,
    private val migrationsProject: ObservableProperty<MigrationsProjectInfo?>,
    private val dbContext: ObservableProperty<DbContextInfo?>
): com.jetbrains.observables.ObservableCollection<MigrationInfo>() {
    fun initBinding() {
        this.bind(dbContext) {
            if (it != null) {
                val migrationsProjectId = migrationsProject.value!!.id
                val dbContextName = it.fullName
                val migrations = intellijProject.solution.riderEfCoreModel.getAvailableMigrations.runUnderProgress(
                    MigrationsIdentity(migrationsProjectId, dbContextName), intellijProject, EfCoreUiBundle.message("progress.title.loading.available.migrations"),
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