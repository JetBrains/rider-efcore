package me.seclerp.rider.plugins.efcore.features.connections

import com.jetbrains.rider.model.RdProjectDescriptor

interface DbConnectionProvider {
    fun getAvailableConnections(project: RdProjectDescriptor): List<DbConnectionInfo>
}