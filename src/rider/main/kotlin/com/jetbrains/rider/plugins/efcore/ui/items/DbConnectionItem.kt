package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.features.connections.DbConnectionInfo
import icons.DatabaseIcons

class DbConnectionItem(data: DbConnectionInfo)
    : IconItem<DbConnectionInfo>(data.name, data.dbms?.icon ?: DatabaseIcons.Dbms, data)