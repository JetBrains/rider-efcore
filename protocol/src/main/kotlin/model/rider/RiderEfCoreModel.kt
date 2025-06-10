package model.rider

import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*
import com.jetbrains.rd.generator.nova.csharp.CSharp50Generator
import com.jetbrains.rd.generator.nova.kotlin.Kotlin11Generator

@Suppress("unused", "HardCodedStringLiteral")
object RiderEfCoreModel : Ext(SolutionModel.Solution) {
    private val ProjectInfo = basestruct {
        field("id", guid)
        field("name", string)
        field("fullPath", string)
        field("namespace", string)
        field("targetFrameworks", immutableList(TargetFrameworkId))
    }

    private val StartupProjectInfo = structdef extends ProjectInfo
    private val MigrationsProjectInfo = structdef extends ProjectInfo

    private val MigrationsIdentityBase = basestruct {
        field("projectId", guid)
        field("dbContextClassFullName", string)
    }

    private val MigrationsIdentity = structdef extends MigrationsIdentityBase {}
    private val MigrationIdentity = structdef extends MigrationsIdentityBase {
        field("migrationShortName", string)
    }

    private val MigrationInfo = structdef {
        field("dbContextClassFullName", string)
        field("migrationShortName", string)
        field("migrationLongName", string)
        field("migrationFolderAbsolutePath", string)
    }

    private val DbContextInfo = structdef {
        field("name", string)
        field("fullName", string)
    }

    private val EfCorePackage = basestruct {
        field("id", string)
        field("version", string)
    }

    private val ToolsPackageInfo = structdef extends EfCorePackage
    private val DbProviderInfo = structdef extends EfCorePackage

    private val CliToolDefinition = structdef {
        field("version", string)
        field("toolKind", enum {
            +"None"
            +"Local"
            +"Global"
        })
    }

    private val TargetFrameworkId = structdef {
        field("version", TargetFrameworkVersion)
        field("presentableName", string)
    }

    private val TargetFrameworkVersion = structdef {
        field("major", int)
        field("minor", int)
        field("patch", int)
    }

    init {
        setting(CSharp50Generator.Namespace, "Rider.Plugins.EfCore.Rd")
        setting(Kotlin11Generator.Namespace, "com.jetbrains.rider.plugins.efcore.rd")

        property("cliToolsDefinition", CliToolDefinition)
        property("availableStartupProjects", immutableList(StartupProjectInfo))
        property("availableMigrationProjects", immutableList(MigrationsProjectInfo))

        call("hasAvailableMigrations", MigrationsIdentity, bool)
        call("getAvailableMigrations", MigrationsIdentity, immutableList(MigrationInfo))
        call("getMigration", MigrationIdentity, MigrationInfo.nullable)
        call("getAvailableDbContexts", guid, immutableList(DbContextInfo))
        call("getAvailableDbProviders", guid, immutableList(DbProviderInfo))
        call("getAvailableToolPackages", guid, immutableList(ToolsPackageInfo))
        call("refreshDotNetToolsCache", void, void)

        callback("onMissingEfCoreToolsDetected", void, void)
    }
}