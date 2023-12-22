package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.rd.TargetFrameworkId

abstract class BaseTargetFrameworkItem(displayName: String, data: TargetFrameworkId?) :
    IconItem<TargetFrameworkId?>(displayName, null, data)

class TargetFrameworkItem(displayName: String, data: TargetFrameworkId) :
    BaseTargetFrameworkItem(displayName, data)

class DefaultTargetFrameworkItem : BaseTargetFrameworkItem(EfCoreUiBundle.message("default.target.framework"), null)