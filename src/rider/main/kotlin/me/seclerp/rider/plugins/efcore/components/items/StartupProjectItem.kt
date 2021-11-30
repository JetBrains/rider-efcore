package me.seclerp.rider.plugins.efcore.components.items

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.models.StartupProjectData
import java.io.File

class StartupProjectItem(displayName: String, data: StartupProjectData)
    : IconItem<StartupProjectData>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)

