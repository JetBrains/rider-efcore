package model.rider

import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*
import com.jetbrains.rd.generator.nova.csharp.CSharp50Generator
import com.jetbrains.rd.generator.nova.kotlin.Kotlin11Generator

@Suppress("unused")
object RiderEfCoreModel : Ext(SolutionModel.Solution) {
    private val StartupProjectInfo = structdef {
        field("name", string)
        field("fullPath", string)
        field("targetFrameworks", immutableList(string))
    }

    private val MigrationsProjectInfo = structdef {
        field("name", string)
        field("fullPath", string)
    }

    private val MigrationInfo = structdef {
        field("shortName", string)
        field("longName", string)
    }

    init {
        setting(CSharp50Generator.Namespace, "Rider.Plugins.EfCore.Rd")
        setting(Kotlin11Generator.Namespace, "me.seclerp.rider.plugins.efcore.rd")

        call("getAvailableMigrationsProjects", void, immutableList(MigrationsProjectInfo))
        call("getAvailableStartupProjects", void, immutableList(StartupProjectInfo))
        call("hasAvailableMigrations", string, bool)
        call("getAvailableMigrations", string, immutableList(MigrationInfo))
    }
}