package com.jetbrains.rider.plugins.efcore.features.connections.impl

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.io.isFile
import com.jetbrains.rider.model.RdCustomLocation
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionProvider
import com.jetbrains.rider.plugins.efcore.features.shared.services.JsonSerializer
import org.jetbrains.annotations.NonNls
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class AppSettingsConnectionProvider(private val intellijProject: Project) : DbConnectionProvider {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<AppSettingsConnectionProvider>()
    }

    private val serializer = intellijProject.service<JsonSerializer>()

    override fun getAvailableConnections(project: RdProjectDescriptor) =
        buildList {
            val directory = (project.location as RdCustomLocation?)?.customLocation?.let(::Path)?.parent ?: return@buildList
            @NonNls
            val connectionStrings = directory.listDirectoryEntries("appsettings*.json")
                .filter { it.isFile() }
                .map { it.name to serializer.deserializeNode(it.toFile()) }
                .flatMap { (fileName, json) ->
                    json?.let {
                        JsonConnectionStringsManager.getInstance(intellijProject).collectConnectionStrings(fileName, json)
                    } ?: emptyList()
                }
            addAll(connectionStrings)
        }
}

