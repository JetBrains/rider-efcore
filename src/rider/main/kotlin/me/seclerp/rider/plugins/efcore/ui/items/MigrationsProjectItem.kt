package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.features.shared.models.MigrationsProjectData
import java.io.File

class MigrationsProjectItem(displayName: String, data: MigrationsProjectData)
    : IconItem<MigrationsProjectData>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)