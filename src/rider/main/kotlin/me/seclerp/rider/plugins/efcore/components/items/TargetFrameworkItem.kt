package me.seclerp.rider.plugins.efcore.components.items

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.DotnetIconType

class TargetFrameworkItem(displayName: String)
    : IconItem<Unit>(displayName, DotnetIconResolver.resolveForType(DotnetIconType.TARGET_FRAMEWORK), Unit)