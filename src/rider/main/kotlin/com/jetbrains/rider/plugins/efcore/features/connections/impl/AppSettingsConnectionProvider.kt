package com.jetbrains.rider.plugins.efcore.features.connections.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.ijent.extensions.toNioPathOrNull
import com.jetbrains.rider.model.RdCustomLocation
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionProvider
import com.jetbrains.rider.plugins.efcore.features.shared.services.JsonSerializer
import org.jetbrains.annotations.NonNls
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class AppSettingsConnectionProvider(private val intellijProject: Project) : DbConnectionProvider {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<AppSettingsConnectionProvider>()
    }

    private val serializer = JsonSerializer.getInstance()

    override fun getAvailableConnections(project: RdProjectDescriptor) =
        buildList {
            val directory = (project.location as? RdCustomLocation)?.customLocation?.toNioPathOrNull()?.parent ?: return@buildList
            @NonNls
            val connectionStrings = directory.listDirectoryEntries("appsettings*.json")
                .filter { it.isRegularFile() }
                .map { it.name to serializer.deserializeNode(it.toFile()) }
                .flatMap { (fileName, json) ->
                    json?.let {
                        JsonConnectionStringsManager.getInstance(intellijProject).collectConnectionStrings(fileName, json)
                    } ?: emptyList()
                }
            addAll(connectionStrings)
        }
}

