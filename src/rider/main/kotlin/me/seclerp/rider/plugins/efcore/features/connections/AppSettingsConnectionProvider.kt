package me.seclerp.rider.plugins.efcore.features.connections

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.io.isFile
import com.jetbrains.rider.model.RdCustomLocation
import com.jetbrains.rider.model.RdProjectDescriptor
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

@Service
class AppSettingsConnectionProvider : DbConnectionProvider {
    companion object {
        private val json = jacksonObjectMapper()
        fun getInstance(intellijProject: Project) = intellijProject.service<AppSettingsConnectionProvider>()
    }

    override fun getAvailableConnections(project: RdProjectDescriptor) =
        buildList {
            val directory = (project.location as RdCustomLocation?)?.customLocation?.let(::Path)?.parent ?: return@buildList
            val connectionStrings = directory.listDirectoryEntries("appsettings*.json")
                .filter { it.isFile() }
                .map { it.name to json.readTree(it.toFile()) }
                .mapNotNull { (fileName, json) -> (json.get("ConnectionStrings") as ObjectNode?)?.let { fileName to it } }
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

