
package com.jetbrains.rider.plugins.efcore.features.shared.statistics

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.eventLog.events.ObjectDescription
import com.intellij.internal.statistic.eventLog.events.ObjectEventData
import com.intellij.internal.statistic.eventLog.events.ObjectEventField
import com.intellij.internal.statistic.eventLog.events.ObjectListEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.openapi.util.Version
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.database.drop.DropDatabaseCommand
import com.jetbrains.rider.plugins.efcore.features.database.update.UpdateDatabaseCommand
import com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold.ScaffoldDbContextCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.remove.RemoveLastMigrationCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.script.GenerateScriptCommand
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.rd.CliToolDefinition
import com.jetbrains.rider.plugins.efcore.rd.DbProviderInfo
import com.jetbrains.rider.plugins.efcore.rd.ProjectInfo
import com.jetbrains.rider.plugins.efcore.rd.TargetFrameworkVersion
import com.jetbrains.rider.plugins.efcore.rd.ToolKind
import com.jetbrains.rider.plugins.efcore.rd.ToolsPackageInfo
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution

@Suppress("UnstableApiUsage", "HardCodedStringLiteral")
class CommandUsageCollector : CounterUsagesCollector() {
    companion object {
        @JvmStatic
        val GROUP = EventLogGroup("rider.efcore.command", 1)

        private val COMMAND = EventFields.Enum<FusCommandType>("command")
        private val STARTUP_PROJECT = ObjectEventField("startupProject", *FusProject.fields)
        private val MIGRATIONS_PROJECT = ObjectEventField("migrationsProject", *FusProject.fields)
        private val TARGET_FRAMEWORK = ObjectEventField("targetFramework", *FusTargetFramework.fields)
        private val BUILD_CONFIGURATION = EventFields.Enum<FusBuildConfiguration>("buildConfiguration")
        private val NO_BUILD = EventFields.Boolean("noBuild")
        private val ENABLE_DIAGNOSTIC_LOGGING = EventFields.Boolean("enableDiagnosticLogging")
        private val ADDITIONAL_ARGUMENTS_PASSED = EventFields.Boolean("additionalArgumentsPassed")
        private val CLI_TOOLS = ObjectEventField("cliTools", *FusCliTools.fields)
        private val NUGET_TOOLS = ObjectListEventField("nugetTools", *FusToolsPackage.fields)
        private val DB_PROVIDERS = ObjectListEventField("dbProviders", *FusProviderPackage.fields)

        @JvmStatic
        private val startedFields = arrayOf<EventField<*>>(
            COMMAND,
            STARTUP_PROJECT,
            MIGRATIONS_PROJECT,
            TARGET_FRAMEWORK,
            BUILD_CONFIGURATION,
            NO_BUILD,
            ENABLE_DIAGNOSTIC_LOGGING,
            ADDITIONAL_ARGUMENTS_PASSED,
            CLI_TOOLS,
            NUGET_TOOLS,
            DB_PROVIDERS
        )

        private val EXIT_CODE = EventFields.Int("exitCode")

        @JvmStatic
        private val finishedFields = arrayOf<EventField<*>>(
            EXIT_CODE
        )

        private val COMMAND_ACTIVITY = GROUP.registerIdeActivity("execution",
            startEventAdditionalFields = startedFields,
            finishEventAdditionalFields = finishedFields)

        // TODO[seclerp]: Reuse from backend
        private val knownPackagesProviders = mapOf(
            "Microsoft.EntityFrameworkCore.SqlServer" to Pair(FusProviderDatabase.SQL_SERVER, FusProviderVendor.MICROSOFT),
            "Microsoft.EntityFrameworkCore.Sqlite" to Pair(FusProviderDatabase.SQLITE, FusProviderVendor.MICROSOFT),
            "Microsoft.EntityFrameworkCore.InMemory" to Pair(FusProviderDatabase.IN_MEMORY, FusProviderVendor.MICROSOFT),
            "Microsoft.EntityFrameworkCore.Cosmos" to Pair(FusProviderDatabase.COSMOS, FusProviderVendor.MICROSOFT),
            "Npgsql.EntityFrameworkCore.PostgreSQL" to Pair(FusProviderDatabase.POSTGRE_SQL, FusProviderVendor.NPGSQL),
            "Pomelo.EntityFrameworkCore.MySql" to Pair(FusProviderDatabase.MY_SQL, FusProviderVendor.POMELO),
            "MySql.EntityFrameworkCore" to Pair(FusProviderDatabase.MY_SQL, FusProviderVendor.MICROSOFT),
            "Oracle.EntityFrameworkCore" to Pair(FusProviderDatabase.ORACLE_DB, FusProviderVendor.ORACLE),
            "MongoDB.EntityFrameworkCore" to Pair(FusProviderDatabase.MONGO_DB, FusProviderVendor.MONGO_DB),
            "Devart.Data.MySql.EFCore" to Pair(FusProviderDatabase.MY_SQL, FusProviderVendor.DEVART),
            "Devart.Data.Oracle.EFCore" to Pair(FusProviderDatabase.ORACLE_DB, FusProviderVendor.DEVART),
            "Devart.Data.PostgreSql.EFCore" to Pair(FusProviderDatabase.POSTGRE_SQL, FusProviderVendor.DEVART),
            "Devart.Data.SQLite.EFCore" to Pair(FusProviderDatabase.SQLITE, FusProviderVendor.DEVART),
            "MASES.EntityFrameworkCore.KNet" to Pair(FusProviderDatabase.KAFKA, FusProviderVendor.MASES_GROUP),
            "InterBase" to Pair(FusProviderDatabase.INTERBASE, FusProviderVendor.INTERBASE),
            "FirebirdSql.EntityFrameworkCore.Firebird" to Pair(FusProviderDatabase.FIREBIRD, FusProviderVendor.CINCURANET),
            "IBM.EntityFrameworkCore" to Pair(FusProviderDatabase.DB2_INFORMIX, FusProviderVendor.IBM),
            "IBM.EntityFrameworkCore-lnx" to Pair(FusProviderDatabase.DB2_INFORMIX, FusProviderVendor.IBM),
            "IBM.EntityFrameworkCore-osx" to Pair(FusProviderDatabase.DB2_INFORMIX, FusProviderVendor.IBM),
            "EntityFrameworkCore.Jet" to Pair(FusProviderDatabase.MS_ACCESS, FusProviderVendor.CIRRUS_RED_ORG),
            "Google.Cloud.EntityFrameworkCore.Spanner" to Pair(FusProviderDatabase.GOOGLE_SPANNER, FusProviderVendor.CLOUD_SPANNER_ECOSYSTEM),
            "Teradata.EntityFrameworkCore" to Pair(FusProviderDatabase.TERADATA, FusProviderVendor.TERADATA)
        )

        // TODO[seclerp]: Reuse from backend
        private val knownToolsPackages = mapOf(
            "Microsoft.EntityFrameworkCore.Tools" to FusToolsPackageKind.EFCORE_TOOLS,
            "Microsoft.EntityFrameworkCore.Design" to FusToolsPackageKind.EFCORE_DESIGN,
        )

        suspend fun withCommandActivity(project: com.intellij.openapi.project.Project, command: DialogCommand, executor: suspend () -> CliCommandResult?) {
            val model = project.solution.riderEfCoreModel

            val startupProject = command.common.startupProject
            val migrationsProject = command.common.migrationsProject
            val tfm = FusTargetFramework.create(command.common.targetFramework?.version)
            val cliTools = FusCliTools.create(model.cliToolsDefinition.valueOrNull)
            val toolsPackages = model.getAvailableToolPackages.startSuspending(startupProject.id)
                .map { FusToolsPackage.create(it) }
            val providersPackages = model.getAvailableDbProviders.startSuspending(migrationsProject.id)
                .map { FusProviderPackage.create(it) }

            fun createCommandType(): FusCommandType {
                return when (command) {
                    is AddMigrationCommand -> FusCommandType.ADD_MIGRATION
                    is RemoveLastMigrationCommand -> FusCommandType.REMOVE_LAST_MIGRATION
                    is GenerateScriptCommand -> FusCommandType.GENERATE_SCRIPT
                    is DropDatabaseCommand -> FusCommandType.DROP_DATABASE
                    is UpdateDatabaseCommand -> FusCommandType.UPDATE_DATABASE
                    is ScaffoldDbContextCommand -> FusCommandType.SCAFFOLD_DB_CONTEXT
                    else -> FusCommandType.OTHER
                }
            }

            fun createBuildConfigurationData(): FusBuildConfiguration {
                return when (command.common.buildConfiguration.lowercase()) {
                    "debug" -> FusBuildConfiguration.DEBUG
                    "release" -> FusBuildConfiguration.RELEASE
                    else -> FusBuildConfiguration.OTHER
                }
            }

            val activity = COMMAND_ACTIVITY.started(project) {
                listOf<EventPair<*>>(
                    COMMAND.with(createCommandType()),
                    STARTUP_PROJECT.with(FusProject.create(startupProject)),
                    MIGRATIONS_PROJECT.with(FusProject.create(migrationsProject)),
                    TARGET_FRAMEWORK.with(tfm),
                    BUILD_CONFIGURATION.with(createBuildConfigurationData()),
                    NO_BUILD.with(command.common.noBuild),
                    ENABLE_DIAGNOSTIC_LOGGING.with(command.common.enableDiagnosticLogging),
                    ADDITIONAL_ARGUMENTS_PASSED.with(command.common.additionalArguments.trim().isNotEmpty()),
                    CLI_TOOLS.with(cliTools),
                    NUGET_TOOLS.with(toolsPackages),
                    DB_PROVIDERS.with(providersPackages)
                )
            }

            var result: CliCommandResult? = null
            try {
                result = executor()
            }
            finally {
                activity.finished { listOf(EXIT_CODE.with(result?.exitCode ?: -1)) }
            }
        }
    }

    override fun getGroup() = GROUP

    private object FusProject {
        private val TARGET_FRAMEWORKS = ObjectListEventField("targetFrameworks", *FusTargetFramework.fields)
        val fields = arrayOf(
            TARGET_FRAMEWORKS
        )
        fun create(projectData: ProjectInfo): ObjectEventData {
            return ObjectEventData(
                TARGET_FRAMEWORKS.with(projectData.targetFrameworks.map { FusTargetFramework.create(it.version) })
            )
        }
    }

    private enum class FusCommandType {
        ADD_MIGRATION,
        REMOVE_LAST_MIGRATION,
        GENERATE_SCRIPT,
        DROP_DATABASE,
        UPDATE_DATABASE,
        SCAFFOLD_DB_CONTEXT,

        OTHER
    }

    private object FusToolsPackage {
        private val VERSION = EventFields.VersionByObject
        private val KIND = EventFields.Enum<FusToolsPackageKind>("kind")
        val fields = arrayOf(
            VERSION,
            KIND
        )
        fun create(toolsPackageInfo: ToolsPackageInfo): ObjectEventData {
            return ObjectEventData(
                VERSION.with(Version.parseVersion(toolsPackageInfo.version)),
                KIND.with(knownToolsPackages[toolsPackageInfo.id] ?: FusToolsPackageKind.OTHER)
            )
        }
    }

    private object FusProviderPackage {
        private val VERSION = EventFields.VersionByObject
        private val DATABASE = EventFields.Enum<FusProviderDatabase>("database")
        private val VENDOR = EventFields.Enum<FusProviderVendor>("vendor")
        val fields = arrayOf(
            VERSION,
            DATABASE,
            VENDOR
        )
        fun create(toolsPackageInfo: DbProviderInfo): ObjectEventData {
            val (databaseData, vendorData) = knownPackagesProviders[toolsPackageInfo.id]
                ?: Pair(FusProviderDatabase.OTHER, FusProviderVendor.OTHER)
            return ObjectEventData(
                VERSION.with(Version.parseVersion(toolsPackageInfo.version)),
                DATABASE.with(databaseData),
                VENDOR.with(vendorData)
            )
        }
    }

    private object FusTargetFramework : ObjectDescription() {
        val VERSION = EventFields.VersionByObject
        val fields = arrayOf(
            VERSION,
        )

        fun create(versionData: Version?): ObjectEventData {
            return ObjectEventData(
                VERSION.with(versionData)
            )
        }

        fun create(versionData: TargetFrameworkVersion?): ObjectEventData {
            return ObjectEventData(
                VERSION.with(versionData?.let { Version(it.major, it.minor, it.patch) })
            )
        }
    }

    private object FusCliTools {
        val VERSION = EventFields.VersionByObject
        val KIND = EventFields.Enum<FusCliToolsKind>("kind")
        val fields = arrayOf(
            VERSION,
            KIND,
        )

        fun create(cliTools: CliToolDefinition?): ObjectEventData {
            return ObjectEventData(
                VERSION.with(cliTools?.version?.let(Version::parseVersion)),
                KIND.with(cliTools?.toolKind?.let(::mapKind) ?: FusCliToolsKind.NONE)
            )
        }

        private fun mapKind(rdKind: ToolKind): FusCliToolsKind {
            return when (rdKind) {
                ToolKind.Local -> FusCliToolsKind.LOCAL
                ToolKind.Global -> FusCliToolsKind.GLOBAL
                ToolKind.None -> FusCliToolsKind.NONE
            }
        }
    }

    private enum class FusToolsPackageKind {
        EFCORE_TOOLS,
        EFCORE_DESIGN,

        OTHER
    }

    private enum class FusProviderDatabase {
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

    private enum class FusProviderVendor {
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

    private enum class FusBuildConfiguration {
        DEBUG,
        RELEASE,

        OTHER
    }

    private enum class FusCliToolsKind {
        LOCAL,
        GLOBAL,
        NONE
    }
}
