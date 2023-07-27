pluginManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        maven("https://cache-redirector.jetbrains.com/plugins.gradle.org")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

rootProject.name = "rider-efcore"

include("protocol")