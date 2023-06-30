package com.jetbrains.rider.plugins.efcore.ui.items

import com.jetbrains.rider.plugins.efcore.ui.DotnetIconResolver
import com.jetbrains.rider.plugins.efcore.ui.DotnetIconType

class BuildConfigurationItem(displayName: String)
    : IconItem<String?>(displayName, DotnetIconResolver.resolveForType(DotnetIconType.BUILD_CONFIGURATION), displayName)

