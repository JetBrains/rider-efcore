package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.ui.DotnetIconResolver
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconType

class MigrationItem(name: String)
    : IconItem<String>(name, DotnetIconResolver.resolveForType(DotnetIconType.CSHARP_CLASS), name)