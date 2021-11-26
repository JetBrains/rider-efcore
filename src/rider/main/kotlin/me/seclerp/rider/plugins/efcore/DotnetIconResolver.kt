package me.seclerp.rider.plugins.efcore

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object DotnetIconResolver {
    fun resolveForExtension(extension: String): Icon? {
        val iconPath = when (extension) {
            "csproj" -> "resharper/ProjectModel/CsharpCoreProject.svg"
            "cs" -> "resharper/PsiCSharp/Csharp.svg"
            "fsproj" -> "resharper/ProjectModel/FsharpCoreProject.svg"
            else -> null
        } ?: return null

        return IconLoader.getIcon(iconPath, javaClass)
    }
}