package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.rd.DbContextInfo
import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType

class DbContextItem(displayName: String, data: DbContextInfo)
    : IconItem<DbContextInfo>(displayName, DotnetIconResolver.resolveForLanguage(data.language), data)