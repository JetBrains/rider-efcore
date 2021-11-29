package me.seclerp.rider.plugins.efcore.rd

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.components.IconItem
import me.seclerp.rider.plugins.efcore.models.StartupProjectData
import java.io.File

fun MigrationsProjectInfo.toIconItem(): IconItem<String> =
    IconItem(
        name,
        DotnetIconResolver.resolveForExtension(File(fullPath).extension)!!,
        fullPath
    )

fun StartupProjectInfo.toIconItem(): IconItem<StartupProjectData> =
    IconItem(
        name,
        DotnetIconResolver.resolveForExtension(File(fullPath).extension)!!,
        StartupProjectData(fullPath, targetFrameworks)
    )