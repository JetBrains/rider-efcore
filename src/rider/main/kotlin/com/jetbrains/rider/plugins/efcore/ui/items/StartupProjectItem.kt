package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.rd.StartupProjectInfo
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconResolver
import java.io.File

class StartupProjectItem(displayName: String, data: StartupProjectInfo)
    : IconItem<StartupProjectInfo>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)

