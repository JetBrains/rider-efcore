package model.rider

import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*
import com.jetbrains.rd.generator.nova.csharp.CSharp50Generator
import com.jetbrains.rd.generator.nova.kotlin.Kotlin11Generator

@Suppress("unused")
object RiderEfCoreModel : Ext(SolutionModel.Solution) {
    private val StartupProjectInfo = structdef {
        field("id", guid)
        field("name", string)
        field("fullPath", string)
        field("targetFrameworks", immutableList(string))
        field("namespace", string)
    }

    private val MigrationsProjectInfo = structdef {
        field("id", guid)
        field("name", string)
        field("fullPath", string)
        field("namespace", string)
    }

    private val MigrationsIdentity = structdef {
        field("projectName", string)
        field("dbContextClassFullName", string)
    }

    private val MigrationInfo = structdef {
        field("dbContextClassFullName", string)
        field("migrationShortName", string)
        field("migrationLongName", string)
        field("migrationFolderAbsolutePath", string)
        field("language", language)
    }

    private val DbContextInfo = structdef {
        field("name", string)
        field("fullName", string)
        field("language", language)
    }

    private val EfToolDefinition = structdef {
        field("version", string)
        field("toolKind", enum {
            +"None"
            +"Local"
            +"Global"
        })
    }

    private val language = enum {
        +"Unknown"
        +"CSharp"
        +"FSharp"
    }

    init {
        setting(CSharp50Generator.Namespace, "Rider.Plugins.EfCore.Rd")
        setting(Kotlin11Generator.Namespace, "me.seclerp.rider.plugins.efcore.rd")

        property("efToolsDefinition", EfToolDefinition)
        property("availableStartupProjects", immutableList(StartupProjectInfo))
        property("availableMigrationProjects", immutableList(MigrationsProjectInfo))

        call("hasAvailableMigrations", MigrationsIdentity, bool)
        call("getAvailableMigrations", MigrationsIdentity, immutableList(MigrationInfo))
        call("getAvailableDbContexts", string, immutableList(DbContextInfo))

        callback("onMissingEfCoreToolsDetected", void, void)
    }
}