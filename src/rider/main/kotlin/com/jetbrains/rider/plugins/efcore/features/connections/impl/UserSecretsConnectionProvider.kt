package com.jetbrains.rider.plugins.efcore.features.connections.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.intellij.ide.customize.transferSettings.db.WindowsEnvVariables
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.projectView.nodes.getUserData
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionProvider
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.features.shared.services.JsonSerializer
import org.jetbrains.annotations.NonNls
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class UserSecretsConnectionProvider(private val intellijProject: Project) : DbConnectionProvider {
    companion object {
        @NonNls
        private val userSecretsFolder = if (SystemInfo.isWindows)
            Path(WindowsEnvVariables.applicationData, "Microsoft", "UserSecrets")
        else
            Path(System.getenv("HOME"), ".microsoft", "usersecrets")
        fun getInstance(intellijProject: Project) = intellijProject.service<UserSecretsConnectionProvider>()
    }

    private val serializer = intellijProject.service<JsonSerializer>()

    override fun getAvailableConnections(project: RdProjectDescriptor) =
        buildList {
            val userSecretsId = project.getUserData("UserSecretsId") ?: return@buildList
            val userSecretsFile = userSecretsFolder.resolve(userSecretsId).resolve("secrets.json").toFile()
            if (!userSecretsFile.exists() || !userSecretsFile.isFile)
                return@buildList
            val userSecretsJson = serializer.deserializeNode(userSecretsFile) ?: return@buildList
            addAll(JsonConnectionStringsManager.getInstance(intellijProject).collectConnectionStrings(EfCoreUiBundle.message("source.user.secrets"), userSecretsJson))
        }.toList()
}