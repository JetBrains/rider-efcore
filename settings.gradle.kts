pluginManagement {
    val rdVersion: String by settings
    repositories {
        if (rdVersion == "SNAPSHOT")
            mavenLocal()
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        maven("https://cache-redirector.jetbrains.com/plugins.gradle.org")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
    plugins {
        id ("com.jetbrains.rdgen") version rdVersion
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.name) {
                // This required to correctly rd-gen plugin resolution. May be we should switch our naming to match Gradle plugin naming convention.
                "rdgen" -> {
                    useModule("com.jetbrains.rd:rd-gen:$rdVersion")
                }
            }
        }
    }
}

rootProject.name = "rider-efcore"

include("protocol")