
package com.jetbrains.rider.plugins.efcore.features.shared.statistics

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.*
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandResult
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import com.jetbrains.rider.projectView.solution
import com.intellij.openapi.util.Version
import com.jetbrains.rider.plugins.efcore.features.database.drop.DropDatabaseCommand
import com.jetbrains.rider.plugins.efcore.features.database.drop.DropDatabaseDataContext
import com.jetbrains.rider.plugins.efcore.features.database.update.UpdateDatabaseCommand
import com.jetbrains.rider.plugins.efcore.features.database.update.UpdateDatabaseDataContext
import com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold.ScaffoldDbContextCommand
import com.jetbrains.rider.plugins.efcore.features.dbcontext.scaffold.ScaffoldDbContextDataContext
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDataContext
import com.jetbrains.rider.plugins.efcore.features.migrations.remove.RemoveLastMigrationCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.remove.RemoveLastMigrationDataContext
import com.jetbrains.rider.plugins.efcore.features.migrations.script.GenerateScriptCommand
import com.jetbrains.rider.plugins.efcore.features.migrations.script.GenerateScriptDataContext
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.DialogCommand
import com.jetbrains.rider.plugins.efcore.rd.*

@Suppress("UnstableApiUsage", "HardCodedStringLiteral")
class CommandUsageCollector : CounterUsagesCollector() {
    companion object {
        @JvmStatic
        val GROUP = EventLogGroup("rider.efcore.command", 1)

        private val COMMAND = EventFields.Enum<FusCommandType>("command")
        private val STARTUP_PROJECT = ObjectEventField("startupProject", FusStartupProject())
        private val MIGRATIONS_PROJECT = ObjectEventField("migrationsProject", FusMigrationsProject())
        private val TARGET_FRAMEWORK = ObjectEventField("targetFramework", FusTargetFramework())
        private val BUILD_CONFIGURATION = EventFields.Enum<FusBuildConfiguration>("buildConfiguration")
        private val NO_BUILD = EventFields.Boolean("noBuild")
        private val ENABLE_DIAGNOSTIC_LOGGING = EventFields.Boolean("enableDiagnosticLogging")
        private val ADDITIONAL_ARGUMENTS_PASSED = EventFields.Boolean("additionalArgumentsPassed")
        private val CLI_TOOLS = ObjectEventField("cliTools", FusCliTools())

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
            val tfm = FusTargetFramework.create(command.common.targetFramework?.let { Version.parseVersion(it) })
            val cliTools = FusCliTools.create(model.cliToolsDefinition.valueOrNull)

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

            suspend fun createStartupProjectData(): ObjectEventData {
                val toolsPackages = model.getAvailableToolPackages.startSuspending(startupProject.id)
                    .map { FusToolsPackage.create(it) }

                return FusStartupProject.create(tfm, toolsPackages)
            }

            suspend fun createMigrationsProjectData(): ObjectEventData {
                val providersPackages = model.getAvailableDbProviders.startSuspending(migrationsProject.id)
                    .map { FusProviderPackage.create(it) }

                return FusMigrationsProject.create(tfm, providersPackages)
            }

            fun createBuildConfigurationData(): FusBuildConfiguration {
                return when (command.common.buildConfiguration.lowercase()) {
                    "debug" -> FusBuildConfiguration.DEBUG
                    "release" -> FusBuildConfiguration.RELEASE
                    else -> FusBuildConfiguration.OTHER
                }
            }

            val startupProjectData = createStartupProjectData()
            val migrationsProjectData = createMigrationsProjectData()

            val activity = COMMAND_ACTIVITY.started(project) {
                listOf<EventPair<*>>(
                    COMMAND.with(createCommandType()),
                    STARTUP_PROJECT.with(startupProjectData),
                    MIGRATIONS_PROJECT.with(migrationsProjectData),
                    TARGET_FRAMEWORK.with(tfm),
                    BUILD_CONFIGURATION.with(createBuildConfigurationData()),
                    NO_BUILD.with(command.common.noBuild),
                    ENABLE_DIAGNOSTIC_LOGGING.with(command.common.enableDiagnosticLogging),
                    ADDITIONAL_ARGUMENTS_PASSED.with(command.common.additionalArguments.trim().isNotEmpty()),
                    CLI_TOOLS.with(cliTools)
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

    private abstract class FusProject : ObjectDescription() {
        var tfm by field(ObjectEventField("tfm", FusTargetFramework()))
    }

    private class FusStartupProject : FusProject() {
        var tools by field(ObjectListEventField("tools", FusToolsPackage()))

        companion object {
            fun create(tfmData: ObjectEventData, toolsData: List<ObjectEventData>): ObjectEventData {
                return build(::FusStartupProject) {
                    tfm = tfmData
                    tools = toolsData
                }
            }
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

    private class FusMigrationsProject : FusProject() {
        var providers by field(ObjectListEventField("providers", FusProviderPackage()))

        companion object {
            fun create(tfmData: ObjectEventData, providersData: List<ObjectEventData>): ObjectEventData {
                return build(::FusMigrationsProject) {
                    tfm = tfmData
                    providers = providersData
                }
            }
        }
    }

    private class FusToolsPackage : ObjectDescription() {
        var version by field(EventFields.VersionByObject)
        var kind by field(EventFields.Enum<FusToolsPackageKind>("kind"))

        companion object {
            fun create(toolsPackageInfo: ToolsPackageInfo): ObjectEventData {
                return build(::FusToolsPackage) {
                    version = Version.parseVersion(toolsPackageInfo.version)
                    kind = knownToolsPackages[toolsPackageInfo.id]
                }
            }
        }
    }

    private class FusProviderPackage : ObjectDescription() {
        var version by field(EventFields.VersionByObject)
        var database by field(EventFields.Enum<FusProviderDatabase>("database"))
        var vendor by field(EventFields.Enum<FusProviderVendor>("vendor"))

        companion object {
            fun create(toolsPackageInfo: DbProviderInfo): ObjectEventData {
                return build(::FusProviderPackage) {
                    val (databaseData, providerData) = knownPackagesProviders[toolsPackageInfo.id]
                        ?: Pair(FusProviderDatabase.OTHER, FusProviderVendor.OTHER)
                    version = Version.parseVersion(toolsPackageInfo.version)
                    database = databaseData
                    vendor = providerData
                }
            }
        }
    }

    private class FusTargetFramework : ObjectDescription() {
        var version by field(EventFields.VersionByObject)

        companion object {
            fun create(versionData: Version?): ObjectEventData {
                return build(::FusTargetFramework) {
                    version = versionData
                }
            }
        }
    }

    private class FusCliTools : ObjectDescription() {
        var version by field(EventFields.VersionByObject)
        var kind by field(EventFields.Enum<FusCliToolsKind>("kind"))

        companion object {
            fun create(cliTools: CliToolDefinition?): ObjectEventData {
                return build(::FusCliTools) {
                    version = cliTools?.version?.let(Version::parseVersion)
                    kind = cliTools?.toolKind?.let(::mapKind)
                }
            }

            private fun mapKind(rdKind: ToolKind): FusCliToolsKind {
                return when (rdKind) {
                    ToolKind.Local -> FusCliToolsKind.LOCAL
                    ToolKind.Global -> FusCliToolsKind.GLOBAL
                    ToolKind.None -> FusCliToolsKind.NONE
                }
            }
        }
    }

    private enum class FusToolsPackageKind {
        EFCORE_TOOLS,
        EFCORE_DESIGN
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
