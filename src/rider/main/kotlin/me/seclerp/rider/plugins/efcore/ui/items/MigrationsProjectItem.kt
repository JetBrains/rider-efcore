package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.rd.MigrationsProjectInfo
import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import java.io.File

class MigrationsProjectItem(displayName: String, data: MigrationsProjectInfo)
    : IconItem<MigrationsProjectInfo>(displayName, DotnetIconResolver.resolveForExtension(File(data.fullPath).extension)!!, data)