package com.jetbrains.rider.plugins.efcore.features.connections

import com.intellij.database.Dbms
import org.jetbrains.annotations.NonNls

data class DbConnectionInfo(
    @NonNls
    val name: String,
    @NonNls
    val connectionString: String,
    val sourceName: String,
    val dbms: Dbms?
)

