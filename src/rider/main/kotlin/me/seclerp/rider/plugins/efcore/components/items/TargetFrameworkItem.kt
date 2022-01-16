package me.seclerp.rider.plugins.efcore.components.items

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.DotnetIconType
import javax.swing.Icon

abstract class BaseTargetFrameworkItem(displayName: String, icon: Icon?, data: String?) :
    IconItem<String?>(displayName, icon, data)

class TargetFrameworkItem(displayName: String, data: String) :
    BaseTargetFrameworkItem(displayName, DotnetIconResolver.resolveForType(DotnetIconType.TARGET_FRAMEWORK), data)

class DefaultTargetFrameworkItem : BaseTargetFrameworkItem("<Default>", null, null)