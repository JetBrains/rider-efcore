package me.seclerp.rider.plugins.efcore.ui.items

import me.seclerp.rider.plugins.efcore.ui.DotnetIconResolver
import me.seclerp.rider.plugins.efcore.ui.DotnetIconType

class BuildConfigurationItem(displayName: String)
    : IconItem<String?>(displayName, DotnetIconResolver.resolveForType(DotnetIconType.BUILD_CONFIGURATION), displayName)

