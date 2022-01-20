package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType

class DbContextItem(displayName: String, data: String)
    : IconItem<String>(displayName, DotnetIconResolver.resolveForType(DotnetIconType.CSHARP_CLASS), data)