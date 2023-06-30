package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.project.Project
import com.jetbrains.observables.ObservableCollection
import com.jetbrains.observables.ObservableProperty
import com.jetbrains.observables.bind
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionsCollector
import com.jetbrains.rider.plugins.efcore.rd.StartupProjectInfo

class ObservableConnections(
    private val intellijProject: Project,
    private val startupProject: ObservableProperty<StartupProjectInfo?>
): com.jetbrains.observables.ObservableCollection<DbConnectionInfo>() {
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