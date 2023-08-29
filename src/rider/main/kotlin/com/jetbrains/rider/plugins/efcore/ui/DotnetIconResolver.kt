package com.jetbrains.rider.plugins.efcore.ui

import icons.ReSharperIcons
import icons.RiderIcons
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object DotnetIconResolver {
    @NonNls
    private const val csprojExtension = "csproj"
    @NonNls
    private const val fsprojExtension = "fsproj"

    @NonNls
    fun resolveForExtension(extension: String): Icon? {
        return when(extension) {
            csprojExtension -> ReSharperIcons.ProjectModel.CsharpProject
            fsprojExtension -> ReSharperIcons.ProjectModel.FsharpCoreProject
            else -> null
        }
    }

    @NonNls
    fun resolveForType(type: DotnetIconType): Icon {
        return when(type) {
            DotnetIconType.BUILD_CONFIGURATION -> ReSharperIcons.ProjectModel.ProjectProperties
            DotnetIconType.TARGET_FRAMEWORK -> RiderIcons.RunConfigurations.Application
            DotnetIconType.CSHARP_CLASS -> ReSharperIcons.PsiCSharp.Csharp
        }
    }
}

enum class DotnetIconType {
    BUILD_CONFIGURATION,
    TARGET_FRAMEWORK,
    CSHARP_CLASS,
}