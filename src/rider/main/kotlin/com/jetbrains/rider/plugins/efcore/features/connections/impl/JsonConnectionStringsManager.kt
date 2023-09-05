package com.jetbrains.rider.plugins.efcore.features.connections.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import org.jetbrains.annotations.NonNls

@Service(Service.Level.PROJECT)
class JsonConnectionStringsManager(intellijProject: Project) {
    companion object {
        fun getInstance(intellijProject: Project) = intellijProject.service<JsonConnectionStringsManager>()

        @NonNls
        private val inlineConnectionRegex = Regex("^ConnectionStrings(:|__)(?<name>.+)\$")
    }

    fun collectConnectionStrings(fileName: String, fileData: JsonNode): List<DbConnectionInfo> = buildList {
        addAll(getConnectionsFromObject(fileName, fileData))
        addAll(getConnectionsFromInlineItems(fileName, fileData))
    }

    /**
     * Returns connection string from a "ConnectionStrings" JSON object.
     * Example:
     * "ConnectionStrings": {
     *   "First": "Value",
     *   "Second": "Value"
     * }
     */
    private fun getConnectionsFromObject(fileName: String, jsonFile: JsonNode): List<DbConnectionInfo> = buildList {
        val obj = jsonFile.get("ConnectionStrings") as ObjectNode? ?: return emptyList()
        obj.fieldNames().forEach { connName ->
            val connString = (obj[connName] as TextNode?)?.textValue()
            if (connString != null)
                add(DbConnectionInfo(connName, connString, fileName, null))
        }
    }

    /**
     * Returns connection string from a "ConnectionStrings"-prefixed JSON fields.
     * Example:
     * "ConnectionStrings:First": "Value"
     * or
     * "ConnectionStrings__Second": "Value"
     */
    private fun getConnectionsFromInlineItems(fileName: String, jsonFile: JsonNode): List<DbConnectionInfo> = buildList {
        jsonFile.fieldNames().forEach { settingsField ->
            when (val match = inlineConnectionRegex.matchEntire(settingsField)) {
                null -> return@forEach
                else -> {
                    val name = match.groups["name"]?.value ?: return@forEach
                    val connection = (jsonFile[settingsField] as TextNode?)?.textValue() ?: return@forEach
                    add(DbConnectionInfo(name, connection, fileName, null))
                }
            }
        }
    }
}