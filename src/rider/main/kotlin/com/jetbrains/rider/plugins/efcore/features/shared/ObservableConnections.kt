package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.observables.ObservableProperty
import com.jetbrains.rider.plugins.efcore.observables.bind
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionsCollector
import com.jetbrains.rider.plugins.efcore.observables.ObservableCollection
import com.jetbrains.rider.plugins.efcore.rd.StartupProjectInfo

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