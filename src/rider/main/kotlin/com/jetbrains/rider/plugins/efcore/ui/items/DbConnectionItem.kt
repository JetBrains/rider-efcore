package com.jetbrains.rider.plugins.efcore.ui.items

import icons.DatabaseIcons
import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo

class DbConnectionItem(data: DbConnectionInfo)
    : IconItem<DbConnectionInfo>(data.name, data.dbms?.icon ?: DatabaseIcons.Dbms, data)