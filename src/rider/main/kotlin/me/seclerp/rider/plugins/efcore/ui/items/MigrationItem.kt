package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.rd.MigrationInfo
import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType

class MigrationItem(displayName: String, data: MigrationInfo)
    : IconItem<MigrationInfo>(displayName, DotnetIconResolver.resolveForLanguage(data.language), data)