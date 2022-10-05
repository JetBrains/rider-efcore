package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.rd.StartupProjectInfo
import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import java.io.File

class StartupProjectItem(displayName: String, data: StartupProjectInfo)
    : IconItem<StartupProjectInfo>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)

