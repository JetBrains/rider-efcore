package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType

class MigrationItem(name: String)
    : IconItem<String>(name, DotnetIconResolver.resolveForType(DotnetIconType.CSHARP_CLASS), name)