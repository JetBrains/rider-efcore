@file:Suppress("HardCodedStringLiteral")

import com.jetbrains.plugin.structure.base.utils.isFile
import org.jetbrains.changelog.exceptions.MissingVersionException
import org.jetbrains.intellij.platform.gradle.Constants
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask
import kotlin.collections.*
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory

repositories {
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    maven("https://cache-redirector.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-repository/snapshots")
    maven("https://cache-redirector.jetbrains.com/maven-central")
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

plugins {
    id("me.filippov.gradle.jvm.wrapper")
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.kotlin.jvm")
}

val dotnetPluginId: String by project
val productVersion: String by project
val pluginVersion: String by project
val buildConfiguration = ext.properties["buildConfiguration"] ?: "Debug"
val sinceProductVersion = run {
    val yearWithMajor = productVersion.substringBefore("-")
    val year = yearWithMajor.substringBefore(".")
    val major = yearWithMajor.substringAfter(".")
    "${year.substring(2)}$major.0".also {
        logger.info("Using since build version: $it")
    }
}

intellijPlatform {
    instrumentCode = false
    buildSearchableOptions = buildConfiguration == "Release"
}

val publishToken: String by project
val publishChannel: String by project

val dotNetSrcDir = File(projectDir, "src/dotnet")

val nuGetSdkPackagesVersionsFile = File(dotNetSrcDir, "RiderSdk.PackageVersions.Generated.props")
val nuGetConfigFile = File(dotNetSrcDir, "nuget.config")

version = pluginVersion

fun File.writeTextIfChanged(content: String) {
    val bytes = content.toByteArray()

    if (!exists() || !readBytes().contentEquals(bytes)) {
        println("Writing $path")
        parentFile.mkdirs()
        writeBytes(bytes)
    }
}

repositories {
    maven { setUrl("https://cache-redirector.jetbrains.com/maven-central") }
}

sourceSets {
    main {
        kotlin.srcDir("src/rider/generated/kotlin")
        kotlin.srcDir("src/rider/main/kotlin")
        resources.srcDir("src/rider/main/resources")
    }
}

dependencies {
    intellijPlatform {
        rider(productVersion, useInstaller = false)
        jetbrainsRuntime()

        bundledPlugin("com.intellij.database")
        bundledPlugin("org.jetbrains.plugins.terminal")

        testFramework(TestFrameworkType.Bundled)
    }
}

val riderModel: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(riderModel.name, provider {
        intellijPlatform.platformPath.resolve("lib/rd/rider-model.jar").also {
            check(it.isFile) {
                "rider-model.jar is not found at $riderModel"
            }
        }
    }) {
        builtBy(Constants.Tasks.INITIALIZE_INTELLIJ_PLATFORM_PLUGIN)
    }
}

tasks {
    wrapper {
        gradleVersion = "8.7"
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-${gradleVersion}-all.zip"
    }

    val riderSdkPath by lazy {
        val path = intellijPlatform.platformPath.resolve("lib/DotNetSdkForRdPlugins").absolute()
        if (!path.isDirectory()) error("$path does not exist or not a directory")

        println("Rider SDK path: $path")
        return@lazy path
    }

    val prepareRiderBuildProps by registering {
        val generatedFile = layout.buildDirectory.file("DotNetSdkPath.generated.props")

        inputs.property("dotNetSdkFile", { riderSdkPath.toString() })
        outputs.file(generatedFile)

        doLast {
            project.file(generatedFile).writeText(
                """<Project>
                |  <PropertyGroup>
                |    <DotNetSdkPath>$riderSdkPath</DotNetSdkPath>
                |  </PropertyGroup>
                |</Project>""".trimMargin()
            )
        }
    }

    val generateNuGetConfig by registering {
        doLast {
            nuGetConfigFile.writeTextIfChanged("""
            <?xml version="1.0" encoding="utf-8"?>
            <!-- Auto-generated from 'generateNuGetConfig' task of old.build_gradle.kts -->
            <!-- Run `gradlew :prepare` to regenerate -->
            <configuration>
            <packageSources>
            <add key="rider-sdk" value="$riderSdkPath" />
            </packageSources>
            </configuration>
            """.trimIndent())
        }
    }

    register("prepare") {
        dependsOn(":protocol:rdgen", generateNuGetConfig, prepareRiderBuildProps)
    }

    val compileDotNet by registering {
        dependsOn(":protocol:rdgen", generateNuGetConfig, prepareRiderBuildProps)
        doLast {
            exec {
                workingDir(dotNetSrcDir)
                executable("dotnet")
                args("build", "-c", buildConfiguration)
            }
        }
    }

    register("testDotNet") {
        dependsOn(compileDotNet)
        doLast {
            val testsDir = dotNetSrcDir.resolve("Tests")
            testsDir.list { entry, name -> entry.isDirectory && name != ".DS_Store" }
                ?.forEach {
                    exec {
                        workingDir(testsDir.absolutePath)
                        executable("dotnet")
                        args("test", "-c", buildConfiguration, it)
                    }
                }
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        dependsOn(":protocol:rdgen")
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    patchPluginXml {
        sinceBuild.set(sinceProductVersion)
        val latestChangelog = try {
            changelog.getUnreleased()
        } catch (_: MissingVersionException) {
            changelog.getLatest()
        }
        changeNotes.set(provider {
            changelog.renderItem(
                latestChangelog
                    .withHeader(false)
                    .withEmptySections(false),
                org.jetbrains.changelog.Changelog.OutputType.HTML
            )
        })
    }

    buildPlugin {
        dependsOn(compileDotNet)

        copy {
            from(layout.buildDirectory.file("distributions/${rootProject.name}-${version}.zip"))
            into("${rootDir}/output")
        }
    }

    runIde {
        // For statistics:
         jvmArgs("-Xmx1500m", "-Didea.is.internal=true", "-Dfus.internal.test.mode=true")
//        jvmArgs("-Xmx1500m")
    }

    test {
        useTestNG()
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        environment["LOCAL_ENV_RUN"] = "true"
    }

    withType<PrepareSandboxTask> {
        dependsOn(compileDotNet)

        val outputFolder = file("$dotNetSrcDir/$dotnetPluginId/bin/$dotnetPluginId/$buildConfiguration")
        val backendFiles = listOf(
            "$outputFolder/$dotnetPluginId.dll",
            "$outputFolder/$dotnetPluginId.pdb"

        // TODO: add additional assemblies
        )

        for (f in backendFiles) {
            from(f) { into("${rootProject.name}/dotnet") }
        }

        doLast {
            for (f in backendFiles) {
                val file = file(f)
                if (!file.exists()) throw RuntimeException("File \"$file\" does not exist")
            }
        }
    }

    publishPlugin {
        token.set(publishToken)
        channels.set(listOf(publishChannel))
    }

    wrapper {
        gradleVersion = "8.7"
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-${gradleVersion}-bin.zip"
    }
}

fun getProductMonorepoRoot(): File? {
    var currentDir = projectDir

    while (currentDir.parent != null) {
        if (currentDir.resolve(".ultimate.root.marker").exists()) {
            return currentDir
        }
        currentDir = currentDir.parentFile
    }

    return null
}