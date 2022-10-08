package me.seclerp.rider.plugins.efcore.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.openapi.util.IconLoader
import icons.ReSharperIcons
import icons.RiderIcons
import me.seclerp.rider.plugins.efcore.rd.Language
import javax.swing.Icon

object DotnetIconResolver {
    fun resolveForExtension(extension: String): Icon? {
        val icon = when (extension) {
            "csproj" -> ReSharperIcons.ProjectModel.CsharpCoreProject
            "fsproj" -> ReSharperIcons.ProjectModel.FsharpCoreProject
            else -> null
        } ?: return null

        return icon
    }

    fun resolveForType(type: DotnetIconType) =
        when (type) {
            DotnetIconType.BUILD_CONFIGURATION -> ReSharperIcons.ProjectModel.ProjectProperties
            DotnetIconType.TARGET_FRAMEWORK -> RiderIcons.RunConfigurations.Application
            DotnetIconType.CSHARP_CLASS -> ReSharperIcons.PsiCSharp.Csharp
            // Fallback to plain text if F# file icon was not found
            DotnetIconType.FSHARP_CLASS -> IconLoader.findIcon("/icons/Fsharp.png", javaClass) ?: AllIcons.FileTypes.Text
        }

    fun resolveForLanguage(language: Language) =
        when (language) {
            Language.CSharp -> ReSharperIcons.PsiCSharp.Csharp
            // Fallback to plain text if F# file icon was not found
            Language.FSharp -> IconLoader.findIcon("/icons/Fsharp.png", javaClass) ?: AllIcons.FileTypes.Text
            Language.Unknown -> AllIcons.FileTypes.Text
        }
}

enum class DotnetIconType {
    BUILD_CONFIGURATION,
    TARGET_FRAMEWORK,
    CSHARP_CLASS,
    FSHARP_CLASS
}