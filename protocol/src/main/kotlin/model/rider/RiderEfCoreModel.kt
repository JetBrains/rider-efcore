package model.rider

import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*

@Suppress("unused")
object RiderEfCoreModel : Ext(SolutionModel.Solution) {
    private val ProjectInfo = structdef {
        field("name", string)
    }

    private val CommonOptions = structdef {
        field("migrationsProject", string)
        field("startupProject", string)
        field("noBuild", bool)
    }

    private val AddMigrationOptions = structdef {
        field("migrationName", string)
        field("migrationsProject", string)
        field("startupProject", string)
        field("noBuild", bool)
    }

    private val OperationResult = structdef {
        field("cliCommand", string)
        field("succeeded", bool)
        field("exitCode", int)
        field("output", string)
    }

    init {
        // Internal
        call("getAvailableMigrationsProjects", void, immutableList(ProjectInfo))
        call("getAvailableStartupProjects", void, immutableList(ProjectInfo))

        // Migrations
        call("addMigration", AddMigrationOptions, OperationResult)
        call("removeLastMigration", CommonOptions, OperationResult)
    }
}