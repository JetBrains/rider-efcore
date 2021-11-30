package me.seclerp.rider.plugins.efcore.components.items

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.models.MigrationsProjectData
import java.io.File

class MigrationsProjectItem(displayName: String, data: MigrationsProjectData)
    : IconItem<MigrationsProjectData>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)