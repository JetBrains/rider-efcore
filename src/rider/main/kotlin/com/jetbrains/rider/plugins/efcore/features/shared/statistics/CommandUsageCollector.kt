
package com.jetbrains.rider.plugins.efcore.features.shared.statistics

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.*
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.intellij.openapi.util.Version
import com.jetbrains.rider.plugins.efcore.rd.MigrationsProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.StartupProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.ToolsPackageInfo

@Suppress("UnstableApiUsage", "HardCodedStringLiteral")
class CommandUsageCollector<TCommand> : CounterUsagesCollector() {
    companion object {
        @JvmStatic
        val GROUP = EventLogGroup("rider.efcore.command", 1)

        private val COMMAND = EventFields.Enum<CommandType>("command")
        private val STARTUP_PROJECT = ObjectEventField("startupProject", StartupProject())
        private val MIGRATIONS_PROJECT = ObjectEventField("migrationsProject", MigrationsProject())
        private val TARGET_FRAMEWORK = ObjectEventField("targetFramework", TargetFramework())
        private val BUILD_CONFIGURATION = EventFields.Enum<BuildConfiguration>("buildConfiguration")
        private val NO_BUILD = EventFields.Boolean("noBuild")
        private val ENABLE_DIAGNOSTIC_LOGGING = EventFields.Boolean("enableDiagnosticLogging")
        private val ADDITIONAL_ARGUMENTS_PASSED = EventFields.Boolean("additionalArgumentsPassed")
        private val CLI_TOOLS = ObjectEventField("cliTools", CliTools())

        @JvmStatic
        private val startedFields = arrayOf<EventField<*>>(
            STARTUP_PROJECT,
            MIGRATIONS_PROJECT,
            TARGET_FRAMEWORK,
            BUILD_CONFIGURATION,
            NO_BUILD,
            ENABLE_DIAGNOSTIC_LOGGING,
            ADDITIONAL_ARGUMENTS_PASSED,
            CLI_TOOLS
        )

        private val EXIT_CODE = EventFields.Int("exitCode")

        @JvmStatic
        private val finishedFields = arrayOf<EventField<*>>(
            EXIT_CODE
        )

        private val COMMAND_ACTIVITY = GROUP.registerIdeActivity("execute",
            startEventAdditionalFields = startedFields,
            finishEventAdditionalFields = finishedFields)

        // TODO[seclerp]: Reuse from backend
        private val knownPackagesProviders = mapOf(
            "Microsoft.EntityFrameworkCore.SqlServer" to Pair(ProviderDatabase.SQL_SERVER, ProviderVendor.MICROSOFT),
            "Microsoft.EntityFrameworkCore.Sqlite" to Pair(ProviderDatabase.SQLITE, ProviderVendor.MICROSOFT),
            "Microsoft.EntityFrameworkCore.InMemory" to Pair(ProviderDatabase.IN_MEMORY, ProviderVendor.MICROSOFT),
            "Microsoft.EntityFrameworkCore.Cosmos" to Pair(ProviderDatabase.COSMOS, ProviderVendor.MICROSOFT),
            "Npgsql.EntityFrameworkCore.PostgreSQL" to Pair(ProviderDatabase.POSTGRE_SQL, ProviderVendor.NPGSQL),
            "Pomelo.EntityFrameworkCore.MySql" to Pair(ProviderDatabase.MY_SQL, ProviderVendor.POMELO),
            "MySql.EntityFrameworkCore" to Pair(ProviderDatabase.MY_SQL, ProviderVendor.MICROSOFT),
            "Oracle.EntityFrameworkCore" to Pair(ProviderDatabase.ORACLE_DB, ProviderVendor.ORACLE),
            "MongoDB.EntityFrameworkCore" to Pair(ProviderDatabase.MONGO_DB, ProviderVendor.MONGO_DB),
            "Devart.Data.MySql.EFCore" to Pair(ProviderDatabase.MY_SQL, ProviderVendor.DEVART),
            "Devart.Data.Oracle.EFCore" to Pair(ProviderDatabase.ORACLE_DB, ProviderVendor.DEVART),
            "Devart.Data.PostgreSql.EFCore" to Pair(ProviderDatabase.POSTGRE_SQL, ProviderVendor.DEVART),
            "Devart.Data.SQLite.EFCore" to Pair(ProviderDatabase.SQLITE, ProviderVendor.DEVART),
            "MASES.EntityFrameworkCore.KNet" to Pair(ProviderDatabase.KAFKA, ProviderVendor.MASES_GROUP),
            "InterBase" to Pair(ProviderDatabase.INTERBASE, ProviderVendor.INTERBASE),
            "FirebirdSql.EntityFrameworkCore.Firebird" to Pair(ProviderDatabase.FIREBIRD, ProviderVendor.CINCURANET),
            "IBM.EntityFrameworkCore" to Pair(ProviderDatabase.DB2_INFORMIX, ProviderVendor.IBM),
            "IBM.EntityFrameworkCore-lnx" to Pair(ProviderDatabase.DB2_INFORMIX, ProviderVendor.IBM),
            "IBM.EntityFrameworkCore-osx" to Pair(ProviderDatabase.DB2_INFORMIX, ProviderVendor.IBM),
            "EntityFrameworkCore.Jet" to Pair(ProviderDatabase.MS_ACCESS, ProviderVendor.CIRRUS_RED_ORG),
            "Google.Cloud.EntityFrameworkCore.Spanner" to Pair(ProviderDatabase.GOOGLE_SPANNER, ProviderVendor.CLOUD_SPANNER_ECOSYSTEM),
            "Teradata.EntityFrameworkCore" to Pair(ProviderDatabase.TERADATA, ProviderVendor.TERADATA)
        )

        // TODO[seclerp]: Reuse from backend
        private val knownToolsPackages = mapOf(
            "Microsoft.EntityFrameworkCore.Tools" to EfCorePackageKind.EFCORE_TOOLS,
            "Microsoft.EntityFrameworkCore.Design" to EfCorePackageKind.EFCORE_DESIGN,
        )
    }

    override fun getGroup() = GROUP

    private abstract class Project : ObjectDescription() {
        var tfm by field(ObjectEventField("tfm", TargetFramework()))
    }

    private class StartupProject : Project() {
        var tools by field(ObjectListEventField("tools", ToolsPackage()))

        companion object {
            fun create(tfmData: ObjectEventData, toolsData: List<ObjectEventData>): ObjectEventData {
                return build(::StartupProject) {
                    tfm = tfmData
                    tools = toolsData
                }
            }
        }
    }

    private class MigrationsProject : Project() {
        var providers by field(ObjectListEventField("providers", ProviderPackage()))

        companion object {
            fun create(tfmData: ObjectEventData, providersData: List<ObjectEventData>): ObjectEventData {
                return build(::MigrationsProject) {
                    tfm = tfmData
                    providers = providersData
                }
            }
        }
    }

    private class ToolsPackage : ObjectDescription() {
        var version by field(EventFields.VersionByObject)
        var kind by field(EventFields.Enum<EfCorePackageKind>("kind"))

        companion object {
            fun create(toolsPackageInfo: ToolsPackageInfo): ObjectEventData {
                return build(::ToolsPackage) {
                    version = Version.parseVersion(toolsPackageInfo.version)
                    kind = knownToolsPackages[toolsPackageInfo.id]
                }
            }
        }
    }

    private class ProviderPackage : ObjectDescription() {
        var version by field(EventFields.VersionByObject)
        var database by field(EventFields.Enum<ProviderDatabase>("database"))
        var vendor by field(EventFields.Enum<ProviderVendor>("vendor"))

        companion object {
            fun create(toolsPackageInfo: ToolsPackageInfo): ObjectEventData {
                return build(::ProviderPackage) {
                    val (databaseData, providerData) = knownPackagesProviders[toolsPackageInfo.id]
                        ?: Pair(ProviderDatabase.OTHER, ProviderVendor.OTHER)
                    version = Version.parseVersion(toolsPackageInfo.version)
                    database = databaseData
                    vendor = providerData
                }
            }
        }
    }

    private class TargetFramework : ObjectDescription() {
        var version by field(EventFields.VersionByObject)

        companion object {
            fun create(versionData: Version?): ObjectEventData {
                return build(::TargetFramework) {
                    version = versionData
                }
            }
        }
    }

    private class CliTools : ObjectDescription() {
        var version by field(EventFields.VersionByObject)
        var kind by field(EventFields.Enum<CliToolsKind>("kind"))
    }

    private enum class EfCorePackageKind {
        EFCORE_TOOLS,
        EFCORE_DESIGN
    }

    private enum class ProviderDatabase {
        SQL_SERVER,
        SQLITE,
        IN_MEMORY,
        COSMOS,
        POSTGRE_SQL,
        MY_SQL,
        ORACLE_DB,
        MONGO_DB,
        KAFKA,
        INTERBASE,
        FIREBIRD,
        DB2_INFORMIX,
        MS_ACCESS,
        GOOGLE_SPANNER,
        TERADATA,

        OTHER
    }

    private enum class ProviderVendor {
        MICROSOFT,
        NPGSQL,
        POMELO,
        ORACLE,
        MONGO_DB,
        DEVART,
        MASES_GROUP,
        INTERBASE,
        CINCURANET,
        IBM,
        CIRRUS_RED_ORG,
        CLOUD_SPANNER_ECOSYSTEM,
        TERADATA,

        OTHER
    }

    private enum class BuildConfiguration {
        DEBUG,
        RELEASE,

        OTHER
    }

    private enum class CliToolsKind {
        LOCAL,
        GLOBAL
    }

    suspend fun withCommandActivity(project: com.intellij.openapi.project.Project, command: CommandType, context: CommonDataContext, executor: suspend () -> CliCommandResult) {
        val startupProject = context.startupProject.value ?: throw Exception()
        val migrationsProject = context.migrationsProject.value ?: throw Exception()

        val model = project.solution.riderEfCoreModel
        val toolsPackages = model.getAvailableToolPackages.startSuspending(startupProject.id)
        val providersPackages = model.getAvailableDbProviders.startSuspending(migrationsProject.id)
        val tfm = TargetFramework.create(context.targetFramework.value?.let { Version.parseVersion(it) })

        val activity = COMMAND_ACTIVITY.started(project) {
            listOf(
                STARTUP_PROJECT.with(),
                MIGRATIONS_PROJECT,
                TARGET_FRAMEWORK,
                BUILD_CONFIGURATION,
                NO_BUILD,
                ENABLE_DIAGNOSTIC_LOGGING,
                ADDITIONAL_ARGUMENTS_PASSED,
                CLI_TOOLS
            )
        }
        try {
            val result = executor()
        }
        finally {
            activity.finished { listOf(CMakeStatisticsCollector.IS_EXTERNAL.with(isExternal)) }
        }

        suspend fun createStartupProject(startupProject: StartupProjectInfo): ObjectEventData {
            val toolsPackages = model.getAvailableToolPackages.startSuspending(startupProject.id)
                .map { ToolsPackage.create(it) }

            return StartupProject.create(tfm, toolsPackages)
        }

        suspend fun createMigrationsProject(migrationsProject: MigrationsProjectInfo): ObjectEventData {
            val toolsPackages = model.getAvailableToolPackages.startSuspending(startupProject.id)
                .map { ToolsPackage.create(it) }

            return StartupProject.create(tfm, toolsPackages)
        }
    }
}
