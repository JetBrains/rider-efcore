package com.jetbrains.rider.plugins.efcore.features.shared.statistics

enum class CommandType {
    ADD_MIGRATION,
    REMOVE_LAST_MIGRATION,
    GENERATE_SCRIPT,
    DROP_DATABASE,
    UPDATE_DATABASE,
    SCAFFOLD_DB_CONTEXT
}