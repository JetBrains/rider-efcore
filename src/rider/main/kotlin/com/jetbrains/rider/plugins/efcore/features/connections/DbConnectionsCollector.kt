package com.jetbrains.rider.plugins.efcore.features.connections

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.features.connections.impl.AppSettingsConnectionProvider
import com.jetbrains.rider.plugins.efcore.features.connections.impl.DataGripConnectionProvider
import com.jetbrains.rider.plugins.efcore.features.connections.impl.LocalSettingsConnectionProvider
import com.jetbrains.rider.plugins.efcore.features.connections.impl.UserSecretsConnectionProvider
import com.jetbrains.rider.projectView.workspace.findProjects
import java.util.*

@Service(Service.Level.PROJECT)
@Suppress("UnstableApiUsage")
internal class DbConnectionsCollector(private val intellijProject: Project) {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<DbConnectionsCollector>()
    }

    private val providers = listOf(
        AppSettingsConnectionProvider.getInstance(intellijProject),
        UserSecretsConnectionProvider.getInstance(intellijProject),
        LocalSettingsConnectionProvider.getInstance(intellijProject),
        DataGripConnectionProvider.getInstance(intellijProject)
    )

    fun collect(projectId: UUID): List<DbConnectionInfo> {
        val project = WorkspaceModel.getInstance(intellijProject)
            .findProjects()
            .filter { it.descriptor is RdProjectDescriptor }
            .map { it.descriptor as RdProjectDescriptor }
            .firstOrNull { it.originalGuid == projectId }
            ?: return emptyList()

        return providers.flatMap { it.getAvailableConnections(project) }
    }
}