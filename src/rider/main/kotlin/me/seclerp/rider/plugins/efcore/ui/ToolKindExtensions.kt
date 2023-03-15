package me.seclerp.rider.plugins.efcore.ui

import me.seclerp.rider.plugins.efcore.EfCoreUiBundle
import me.seclerp.rider.plugins.efcore.rd.ToolKind

fun ToolKind.localize(): String =
    when (this) {
        ToolKind.None -> EfCoreUiBundle.message("tool.kind.none")
        ToolKind.Local -> EfCoreUiBundle.message("tool.kind.local")
        ToolKind.Global -> EfCoreUiBundle.message("tool.kind.global")
    }