package com.jetbrains.rider.plugins.efcore.features.connections.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.RdCustomLocation
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionProvider
import com.jetbrains.rider.plugins.efcore.features.shared.services.JsonSerializer
import kotlin.io.path.Path

/**
 * Azure Functions-specific file local.settings.json
 */
@Service(Service.Level.PROJECT)
class LocalSettingsConnectionProvider(private val intellijProject: Project) : DbConnectionProvider {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<LocalSettingsConnectionProvider>()
    }

    private val serializer = JsonSerializer.Companion.getInstance()

    override fun getAvailableConnections(project: RdProjectDescriptor) =
        buildList {
            val directory = (project.location as? RdCustomLocation)?.customLocation?.let(::Path)?.parent ?: return@buildList
            val localSettingsFile = directory.resolve("local.settings.json").toFile()
            if (!localSettingsFile.exists() || !localSettingsFile.isFile)
                return@buildList
            val localSettingsJson = serializer.deserializeNode(localSettingsFile) ?: return@buildList
            addAll(JsonConnectionStringsManager.getInstance(intellijProject).collectConnectionStrings("local.settings.json", localSettingsJson))
        }
}