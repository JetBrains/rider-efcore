package me.seclerp.rider.plugins.efcore.features.connections

import com.intellij.database.Dbms

data class DbConnectionInfo(
    val name: String,
    val connectionString: String,
    val sourceName: String,
    val dbms: Dbms?
)

