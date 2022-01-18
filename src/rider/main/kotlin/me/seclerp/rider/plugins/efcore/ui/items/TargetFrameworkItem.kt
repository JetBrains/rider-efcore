package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType
import javax.swing.Icon

abstract class BaseTargetFrameworkItem(displayName: String, icon: Icon?, data: String?) :
    IconItem<String?>(displayName, icon, data)

class TargetFrameworkItem(displayName: String, data: String) :
    BaseTargetFrameworkItem(displayName, DotnetIconResolver.resolveForType(DotnetIconType.TARGET_FRAMEWORK), data)

class DefaultTargetFrameworkItem : BaseTargetFrameworkItem("<Default>", null, null)