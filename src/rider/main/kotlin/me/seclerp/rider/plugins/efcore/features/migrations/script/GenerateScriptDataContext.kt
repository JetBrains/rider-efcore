package me.seclerp.rider.plugins.efcore.features.migrations.script

import com.intellij.openapi.project.Project
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.observables.ObservableProperty
import me.seclerp.observables.bind
import me.seclerp.observables.bindNullable
import me.seclerp.rider.plugins.efcore.features.shared.dialog.CommonDataContext
import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsIdentity
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel

class GenerateScriptDataContext(
    intellijProject: Project,
    commonCtx: CommonDataContext,
    beModel: RiderEfCoreModel
) {
    val availableMigrations = ObservableProperty(listOf<MigrationInfo>())

    val availableFromMigrations = ObservableProperty(listOf<String>())
    val availableToMigrations = ObservableProperty(listOf<String>())

    val fromMigration = ObservableProperty<String>(null)
    val toMigration = ObservableProperty<String>(null)
    val outputFilePath = ObservableProperty("script.sql")
    val idempotent = ObservableProperty(false)
    val noTransactions = ObservableProperty(false)

    init {
        commonCtx.dbContext.afterChange {
            val migrationProjectName = commonCtx.migrationsProject.notNullValue.name
            val dbContextName = it!!.fullName
            val migrations = beModel.getAvailableMigrations.runUnderProgress(
                MigrationsIdentity(migrationProjectName, dbContextName), intellijProject, "Loading available migrations...",
                isCancelable = true,
                throwFault = true
            )

            if (migrations != null) {
                availableMigrations.value = migrations
            }
        }

        availableFromMigrations.bind(availableMigrations) {
            buildList {
                addAll(it.map { it.migrationLongName })
                add("0")
            }
        }

        availableToMigrations.bind(availableMigrations) {
            buildList {
                addAll(it.map { it.migrationLongName })
            }
        }

        fromMigration.bindNullable(availableFromMigrations) { it?.lastOrNull() }
        toMigration.bindNullable(availableToMigrations) { it?.firstOrNull() }
    }
}