package com.jetbrains.rider.plugins.efcore.ui.items

import com.intellij.icons.AllIcons
import com.jetbrains.rider.plugins.efcore.rd.DbProviderInfo
import icons.DatabaseIcons
import org.jetbrains.annotations.NonNls

class DbProviderItem(data: DbProviderInfo)
    : IconItem<DbProviderInfo>(data.id, resolveKnownIcon(data.id), data) {
        companion object {
            @NonNls
            private fun resolveKnownIcon(packageId: String) =
                when (packageId.lowercase()) {
                    "microsoft.entityframeworkcore.sqlserver" -> AllIcons.Providers.SqlServer
                    "microsoft.entityframeworkcore.sqlite" -> AllIcons.Providers.Sqlite
                    "microsoft.entityframeworkcore.cosmos" -> AllIcons.Providers.Azure
                    "npgsql.entityframeworkcore.postgresql" -> AllIcons.Providers.Postgresql
                    "pomelo.entityframeworkcore.mysql" -> AllIcons.Providers.Mysql
                    "mysql.entityframeworkcore" -> AllIcons.Providers.Mysql
                    "oracle.entityframeworkcore" -> AllIcons.Providers.Oracle
                    "devart.data.mysql.efcore" -> AllIcons.Providers.Mysql
                    "devart.data.oracle.efcore" -> AllIcons.Providers.Oracle
                    "devart.data.postgresql.efcore" -> AllIcons.Providers.Postgresql
                    "devart.data.sqlite.efcore" -> AllIcons.Providers.Sqlite
                    "firebirdsql.entityframeworkcore.firebird" -> AllIcons.Providers.Firebird
                    "ibm.entityframeworkcore" -> AllIcons.Providers.DB2
                    "ibm.entityframeworkcore-lnx" -> AllIcons.Providers.DB2
                    "ibm.entityframeworkcore-osx" -> AllIcons.Providers.DB2
                    "google.cloud.entityframeworkcore.spanner" -> AllIcons.Providers.GoogleCloudSpanner
                    "teradata.entityframeworkcore" -> AllIcons.Providers.Teradata

                    "microsoft.entityframeworkcore.inmemory" -> DatabaseIcons.Dbms
                    "entityframeworkcore.jet" -> DatabaseIcons.Dbms
                    "filecontextcore" -> DatabaseIcons.Dbms
                    "filebasecontext" -> DatabaseIcons.Dbms
                    else -> DatabaseIcons.Dbms
                }
        }
    }