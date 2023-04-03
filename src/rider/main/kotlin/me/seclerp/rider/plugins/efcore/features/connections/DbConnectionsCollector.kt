package me.seclerp.rider.plugins.efcore.features.connections

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.projectView.workspace.findProjects
import me.seclerp.rider.plugins.efcore.features.connections.impl.AppSettingsConnectionProvider
import me.seclerp.rider.plugins.efcore.features.connections.impl.DataGripConnectionProvider
import me.seclerp.rider.plugins.efcore.features.connections.impl.UserSecretsConnectionProvider
import java.util.*

@Service
@Suppress("UnstableApiUsage")
class DbConnectionsCollector(private val intellijProject: Project) {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<DbConnectionsCollector>()
    }

    private val providers = listOf(
        AppSettingsConnectionProvider.getInstance(intellijProject),
        UserSecretsConnectionProvider.getInstance(intellijProject),
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