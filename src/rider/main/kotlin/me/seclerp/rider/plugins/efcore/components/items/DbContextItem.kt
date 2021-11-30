package me.seclerp.rider.plugins.efcore.components.items

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.DotnetIconType

class DbContextItem(displayName: String, data: String)
    : IconItem<String>(displayName, DotnetIconResolver.resolveForType(DotnetIconType.BUILD_CONFIGURATION), data)