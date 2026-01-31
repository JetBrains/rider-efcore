package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.jetbrains.observables.ObservableProperty
import com.jetbrains.observables.bind
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.rd.DbProviderInfo
import com.jetbrains.rider.plugins.efcore.rd.MigrationsProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress

class ObservableDbProviders(
    private val intellijProject: Project,
    private val migrationsProject: ObservableProperty<MigrationsProjectInfo?>
): com.jetbrains.observables.ObservableCollection<DbProviderInfo>() {
    fun initBinding() {
        this.bind(migrationsProject) {
            if (it != null) {
                val projectId = it.id
                val dbProviders = intellijProject.solution.riderEfCoreModel.getAvailableDbProviders.runUnderProgress(
                    projectId, intellijProject, EfCoreUiBundle.message("progress.title.loading.available.db.providers"),
                    isCancelable = true,
                    throwFault = true
                )

                dbProviders ?: listOf()
            } else {
                listOf()
            }
        }
    }
}