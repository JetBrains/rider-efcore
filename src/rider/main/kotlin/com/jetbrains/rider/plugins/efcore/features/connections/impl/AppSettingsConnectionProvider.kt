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

@Service
class AppSettingsConnectionProvider(intellijProject: Project) : DbConnectionProvider {
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
                .mapNotNull { (fileName, json) -> (json?.get("ConnectionStrings") as ObjectNode?)?.let { fileName to it } }
                .flatMap { (fileName, obj) ->
                    obj.fieldNames().asSequence().map { connName ->
                        (obj[connName] as TextNode?)?.let { node -> Triple(fileName, connName, node.textValue()) }
                    }
                }
                .filterNotNull()
                .map { (fileName, connName, connString) -> DbConnectionInfo(connName, connString, fileName, null) }

            addAll(connectionStrings)
        }.toList()
}

