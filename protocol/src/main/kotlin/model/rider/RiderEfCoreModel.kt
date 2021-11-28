package model.rider

import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*

@Suppress("unused")
object RiderEfCoreModel : Ext(SolutionModel.Solution) {
    private val ProjectInfo = structdef {
        field("name", string)
        field("fullPath", string)
    }

    private val MigrationInfo = structdef {
        field("shortName", string)
        field("longName", string)
    }

    init {
        call("getAvailableMigrationsProjects", void, immutableList(ProjectInfo))
        call("getAvailableStartupProjects", void, immutableList(ProjectInfo))
        call("hasAvailableMigrations", string, bool)
        call("getAvailableMigrations", string, immutableList(MigrationInfo))
    }
}