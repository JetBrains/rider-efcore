package com.jetbrains.rider.plugins.efcore.features.connections.impl

import com.intellij.database.Dbms
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionProvider
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
class DataGripConnectionProvider(private val intellijProject: Project) : DbConnectionProvider {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<DataGripConnectionProvider>()
    }
    override fun getAvailableConnections(project: RdProjectDescriptor) = buildList {
        LocalDataSourceManager.getInstance(intellijProject).dataSources.forEach {
            val connString = generateConnectionString(it)
            if (connString != null)
                add(DbConnectionInfo(it.name, connString, EfCoreUiBundle.message("source.data.sources"), it.dbms))
        }
    }

    @NonNls
    private fun generateConnectionString(source: LocalDataSource) =
        when (source.dbms) {
            Dbms.SQLITE -> "Data Source=${source.url?.removePrefix("jdbc:sqlite:")}"
            else -> null
        }
}