package me.seclerp.rider.plugins.efcore.components.items

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.DotnetIconType
import javax.swing.Icon

class TargetFrameworkItem : IconItem<Unit> {
    constructor(displayName: String, icon: Icon?) : super(displayName, icon, Unit)

    constructor(displayName: String) : super(
        displayName,
        DotnetIconResolver.resolveForType(DotnetIconType.TARGET_FRAMEWORK),
        Unit
    )
}