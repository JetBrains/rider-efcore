package me.seclerp.rider.plugins.efcore.ui

import com.intellij.openapi.util.IconLoader
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object DotnetIconResolver {
    @NonNls
    private const val csprojExtension = "csproj"
    @NonNls
    private const val fsprojExtension = "fsproj"

    @NonNls
    fun resolveForExtension(extension: String): Icon? {
        val iconPath = when (extension) {
            csprojExtension -> "resharper/ProjectModel/CsharpCoreProject.svg"
            fsprojExtension -> "resharper/ProjectModel/FsharpCoreProject.svg"
            else -> null
        } ?: return null

        return IconLoader.getIcon(iconPath, javaClass)
    }

    @NonNls
    fun resolveForType(type: DotnetIconType): Icon {
        val iconPath = when (type) {
            DotnetIconType.BUILD_CONFIGURATION -> "resharper/ProjectModel/ProjectProperties.svg"
            DotnetIconType.TARGET_FRAMEWORK -> "rider/runConfigurations/application.svg"
            DotnetIconType.CSHARP_CLASS -> "resharper/PsiCSharp/Csharp.svg"
        }

        return IconLoader.getIcon(iconPath, javaClass)
    }
}

enum class DotnetIconType {
    BUILD_CONFIGURATION,
    TARGET_FRAMEWORK,
    CSHARP_CLASS,
}