package com.jetbrains.rider.plugins.efcore.ui

import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.rd.ToolKind

fun ToolKind.localize(): String =
    when (this) {
        ToolKind.None -> EfCoreUiBundle.message("tool.kind.none")
        ToolKind.Local -> EfCoreUiBundle.message("tool.kind.local")
        ToolKind.Global -> EfCoreUiBundle.message("tool.kind.global")
    }