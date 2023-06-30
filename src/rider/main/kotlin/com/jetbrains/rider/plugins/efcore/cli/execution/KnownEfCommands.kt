package com.jetbrains.rider.plugins.efcore.cli.execution

import org.jetbrains.annotations.NonNls

@NonNls
object KnownEfCommands {
    val dotnet       = "dotnet"
    val ef           = "ef"

    object Migrations {
        val add      = "migrations add"
        val remove   = "migrations remove"
        val bundle   = "migrations bundle"
        val list     = "migrations list"
        val script   = "migrations script"
    }

    object Database {
        val update   = "database update"
        val drop     = "database drop"
    }

    object DbContext {
        val info     = "dbcontext info"
        val list     = "dbcontext list"
        val optimize = "dbcontext optimize"
        val scaffold = "dbcontext scaffold"
        val script   = "dbcontext script"
    }
}