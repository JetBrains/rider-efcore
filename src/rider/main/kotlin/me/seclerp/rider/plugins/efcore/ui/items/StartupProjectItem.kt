package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.features.shared.models.StartupProjectData
import java.io.File

class StartupProjectItem(displayName: String, data: StartupProjectData)
    : IconItem<StartupProjectData>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)

