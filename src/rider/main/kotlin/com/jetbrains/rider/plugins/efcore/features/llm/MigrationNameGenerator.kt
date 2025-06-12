package com.jetbrains.rider.plugins.efcore.features.llm

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDataContext
import javax.swing.Icon

interface MigrationNameGenerator {
    companion object {
        val EP_NAME = ExtensionPointName<MigrationNameGenerator>("rider.plugins.efcore.migrationNameGenerator")
    }

    val title: String
    val icon: Icon

    suspend fun generateName(dataCtx: AddMigrationDataContext): String
}

