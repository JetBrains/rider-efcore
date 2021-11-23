package me.seclerp.rider.plugins.efcore

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object ProjectIconResolver {
    fun resolve(projectFileName: String): Icon? {
        val iconPath = when (projectFileName.substringAfterLast('.', "")) {
            "csproj" -> "resharper/ProjectModel/CsharpCoreProject.svg"
            "fsproj" -> "resharper/ProjectModel/FsharpCoreProject.svg"
            else -> null
        } ?: return null

        return IconLoader.getIcon(iconPath, javaClass)
    }
}