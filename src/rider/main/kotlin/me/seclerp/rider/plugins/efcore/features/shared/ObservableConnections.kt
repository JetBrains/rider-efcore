package me.seclerp.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import me.seclerp.observables.ObservableCollection
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bind
import me.seclerp.rider.plugins.efcore.features.connections.DbConnectionInfo
import me.seclerp.rider.plugins.efcore.features.connections.DbConnectionsCollector
import me.seclerp.rider.plugins.efcore.rd.StartupProjectInfo

class ObservableConnections(
    private val intellijProject: Project,
    private val startupProject: ObservableProperty<StartupProjectInfo?>
): ObservableCollection<DbConnectionInfo>() {
    private val connectionsCollector by lazy { DbConnectionsCollector.getInstance(intellijProject) }
    fun initBinding() {
        this.bind(startupProject) {
            if (it != null) {
                connectionsCollector.collect(it.id)
            } else {
                listOf()
            }
        }
    }
}