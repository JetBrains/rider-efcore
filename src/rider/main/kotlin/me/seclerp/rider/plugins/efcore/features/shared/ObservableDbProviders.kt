package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.observables.ObservableCollection
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bind
import me.seclerp.rider.plugins.efcore.rd.*

class ObservableDbProviders(
    private val intellijProject: Project,
    private val migrationsProject: ObservableProperty<MigrationsProjectInfo?>
): ObservableCollection<DbProviderInfo>() {
    fun initBinding() {
        this.bind(migrationsProject) {
            if (it != null) {
                val projectId = it.id
                val dbProviders = intellijProject.solution.riderEfCoreModel.getAvailableDbProviders.runUnderProgress(
                    projectId, intellijProject, "Loading available DB providers...",
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