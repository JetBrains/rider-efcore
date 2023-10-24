package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconResolver
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconType
import javax.swing.Icon

abstract class BaseTargetFrameworkItem(displayName: String, data: String?) :
    IconItem<String?>(displayName, null, data)

class TargetFrameworkItem(displayName: String, data: String) :
    BaseTargetFrameworkItem(displayName, data)

class DefaultTargetFrameworkItem : BaseTargetFrameworkItem(EfCoreUiBundle.message("default.target.framework"), null)