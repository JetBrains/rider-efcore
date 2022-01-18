package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object DotnetIconResolver {
    fun resolveForExtension(extension: String): Icon? {
        val iconPath = when (extension) {
            "csproj" -> "resharper/ProjectModel/CsharpCoreProject.svg"
            "fsproj" -> "resharper/ProjectModel/FsharpCoreProject.svg"
            else -> null
        } ?: return null

        return IconLoader.getIcon(iconPath, javaClass)
    }

    fun resolveForType(type: DotnetIconType): Icon {
        val iconPath = when (type) {
            DotnetIconType.BUILD_CONFIGURATION -> "resharper/ProjectModel/ProjectProperties.svg"
            DotnetIconType.TARGET_FRAMEWORK -> "rider/runConfigurations/application.svg"
            DotnetIconType.CLASS -> "resharper/PsiCSharp/Csharp.svg"
        }

        return IconLoader.getIcon(iconPath, javaClass)
    }
}

enum class DotnetIconType {
    BUILD_CONFIGURATION,
    TARGET_FRAMEWORK,
    CLASS,
}