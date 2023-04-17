package me.seclerp.rider.plugins.efcore.features.connections.impl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.jetbrains.rider.ideaInterop.welcomeWizard.transferSettingsRider.utilities.WindowsEnvVariables
import com.jetbrains.rider.model.RdProjectDescriptor
import com.jetbrains.rider.projectView.nodes.getUserData
import me.seclerp.rider.plugins.efcore.features.connections.DbConnectionInfo
import me.seclerp.rider.plugins.efcore.features.connections.DbConnectionProvider
import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import org.jetbrains.annotations.NonNls
import kotlin.io.path.Path

@Service
class UserSecretsConnectionProvider : DbConnectionProvider {
    companion object {
        private val json =
            jacksonObjectMapper()
                .enable(JsonParser.Feature.ALLOW_COMMENTS)

        @NonNls
        private val userSecretsFolder = if (SystemInfo.isWindows)
            Path(WindowsEnvVariables.applicationData, "Microsoft", "UserSecrets")
        else
            Path(System.getenv("HOME"), ".microsoft", "usersecrets")
        fun getInstance(intellijProject: Project) = intellijProject.service<UserSecretsConnectionProvider>()
    }

    override fun getAvailableConnections(project: RdProjectDescriptor) =
        buildList {
            val userSecretsId = project.getUserData("UserSecretsId") ?: return@buildList
            val userSecretsFile = userSecretsFolder.resolve(userSecretsId).resolve("secrets.json").toFile()
            if (!userSecretsFile.exists() || !userSecretsFile.isFile)
                return@buildList
            val obj = json.readTree(userSecretsFile).get("ConnectionStrings") as ObjectNode? ?: return@buildList
            obj.fieldNames().forEach { connName ->
                val connString = (obj[connName] as TextNode?)?.textValue()
                if (connString != null)
                    add(DbConnectionInfo(connName, connString, EfCoreUiBundle.message("source.user.secrets"), null))
            }
        }.toList()
}