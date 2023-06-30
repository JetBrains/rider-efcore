package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.rd.DbContextInfo
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconResolver
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconType

class DbContextItem(displayName: String, data: DbContextInfo)
    : IconItem<DbContextInfo>(displayName, DotnetIconResolver.resolveForType(DotnetIconType.CSHARP_CLASS), data)