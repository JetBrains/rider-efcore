package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconResolver
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconType
import javax.swing.Icon

abstract class BaseTargetFrameworkItem(displayName: String, icon: Icon?, data: String?) :
    IconItem<String?>(displayName, icon, data)

class TargetFrameworkItem(displayName: String, data: String) :
    BaseTargetFrameworkItem(displayName, DotnetIconResolver.resolveForType(DotnetIconType.TARGET_FRAMEWORK), data)

class DefaultTargetFrameworkItem : BaseTargetFrameworkItem(EfCoreUiBundle.message("default.target.framework"), null, null)