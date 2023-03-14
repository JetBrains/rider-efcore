package me.seclerp.rider.plugins.efcore.features.connections

import com.intellij.database.Dbms
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.RdProjectDescriptor

@Service
class DataGripConnectionProvider(private val intellijProject: Project) : DbConnectionProvider {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<DataGripConnectionProvider>()
    }
    override fun getAvailableConnections(project: RdProjectDescriptor) = buildList {
        LocalDataSourceManager.getInstance(intellijProject).dataSources.forEach {
            val connString = generateConnectionString(it)
            if (connString != null)
                add(DbConnectionInfo(it.name, connString, "Data sources", it.dbms))
        }
    }

    private fun generateConnectionString(source: LocalDataSource) =
        when (source.dbms) {
            Dbms.SQLITE -> "Data Source=${source.url?.removePrefix("jdbc:sqlite:")}"
            else -> null
        }
}