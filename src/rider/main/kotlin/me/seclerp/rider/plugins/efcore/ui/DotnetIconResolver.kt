package me.seclerp.rider.plugins.efcore.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import icons.ReSharperIcons
import icons.RiderIcons
import javax.swing.Icon

object DotnetIconResolver {
    fun resolveForExtension(extension: String): Icon? {
        val icon = when (extension) {
            "csproj" -> ReSharperIcons.ProjectModel.CsharpCoreProject
            "fsproj" -> ReSharperIcons.ProjectModel.FsharpCoreProject
            else -> null
        } ?: return null

        ReSharperIcons.ProjectModel.Fsharp
        return icon
    }

    fun resolveForType(type: DotnetIconType): Icon {
        val icon = when (type) {
            DotnetIconType.BUILD_CONFIGURATION -> ReSharperIcons.ProjectModel.ProjectProperties
            DotnetIconType.TARGET_FRAMEWORK -> RiderIcons.RunConfigurations.Application
            DotnetIconType.CSHARP_CLASS -> ReSharperIcons.PsiCSharp.Csharp
            // Fallback to plain text if F# file icon was not found
            DotnetIconType.FSHARP_CLASS -> IconLoader.findIcon("/icons/Fsharp.png", javaClass) ?: AllIcons.FileTypes.Text
        }

        return icon
    }
}

enum class DotnetIconType {
    BUILD_CONFIGURATION,
    TARGET_FRAMEWORK,
    CSHARP_CLASS,
    FSHARP_CLASS
}