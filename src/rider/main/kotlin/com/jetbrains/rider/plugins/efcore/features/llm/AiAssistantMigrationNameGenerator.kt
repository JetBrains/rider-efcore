package com.jetbrains.rider.plugins.efcore.features.llm

import com.intellij.ml.llm.MLLlmIcons
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.appender.base.llm.RiderAIChatHost
import com.jetbrains.rider.plugins.appender.base.privacy.RiderProtocolStringPrivacyWrapper
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDataContext

@Suppress("UnstableApiUsage")
class AiAssistantMigrationNameGenerator(project: Project) : MigrationNameGenerator {
    override val title = EfCoreUiBundle.message("generate.migration.name.with.ai.assistant")
    override val icon = MLLlmIcons.AiAssistantColored

    override suspend fun generateName(dataCtx: AddMigrationDataContext): String {
        RiderProtocolStringPrivacyWrapper(dbContextFullName).psString
    }
}