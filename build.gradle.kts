@file:Suppress("HardCodedStringLiteral")

import org.jetbrains.changelog.exceptions.MissingVersionException
import kotlin.collections.*

buildscript {
    repositories {
        maven { setUrl("https://cache-redirector.jetbrains.com/maven-central") }
    }
    dependencies {
        classpath("com.jetbrains.rd:rd-gen:2023.3.0")
    }
}

repositories {
    maven("https://cache-redirector.jetbrains.com/intellij-repository/snapshots")
    maven("https://cache-redirector.jetbrains.com/maven-central")
}

plugins {
    id("me.filippov.gradle.jvm.wrapper") version "0.14.0"
    // https://plugins.gradle.org/plugin/org.jetbrains.changelog
    id("org.jetbrains.changelog") version "2.2.0"
    // https://plugins.gradle.org/plugin/org.jetbrains.intellij
    id("org.jetbrains.intellij") version "1.16.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
}

apply {
    plugin("com.jetbrains.rdgen")
}

dependencies {
    testImplementation("org.testng:testng:7.7.0")
}

val riderPluginId: String by project
val dotnetPluginId: String by project
val productVersion: String by project
val pluginVersion: String by project
val buildConfiguration = ext.properties["buildConfiguration"] ?: "Debug"

val publishToken: String by project
val publishChannel: String by project

val rdLibDirectory: () -> File = { file("${tasks.setupDependencies.get().idea.get().classes}/lib/rd") }
extra["rdLibDirectory"] = rdLibDirectory

val dotNetSrcDir = File(projectDir, "src/dotnet")

val nuGetSdkPackagesVersionsFile = File(dotNetSrcDir, "RiderSdk.PackageVersions.Generated.props")
val nuGetConfigFile = File(dotNetSrcDir, "nuget.config")

val ktOutputRelativePath = "src/rider/main/kotlin/${riderPluginId.replace('.','/').lowercase()}/rd"

val productMonorepoDir = getProductMonorepoRoot()
val monorepoPreGeneratedRootDir by lazy { productMonorepoDir?.resolve("Plugins/_RiderEfCore.Pregenerated") ?: error("Building not in monorepo") }
val monorepoPreGeneratedFrontendDir by lazy {  monorepoPreGeneratedRootDir.resolve("Frontend") }
val monorepoPreGeneratedBackendDir by lazy {  monorepoPreGeneratedRootDir.resolve("BackendModel") }
val ktOutputMonorepoRoot by lazy { monorepoPreGeneratedFrontendDir.resolve(ktOutputRelativePath) }

extra["productMonorepoDir"] = productMonorepoDir

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
        kotlin.srcDir("src/rider/main/kotlin")
        resources.srcDir("src/rider/main/resources")
    }
}

apply(plugin = "com.jetbrains.rdgen")

configure<com.jetbrains.rd.generator.gradle.RdGenExtension> {
    val inMonorepo = productMonorepoDir != null
    val modelDir = file("$projectDir/protocol/src/main/kotlin/model")
    val csOutput =
        if (inMonorepo) monorepoPreGeneratedBackendDir
        else file("$projectDir/src/dotnet/$dotnetPluginId/Rd")
    val ktOutput =
        if (inMonorepo) ktOutputMonorepoRoot
        else file("$projectDir/$ktOutputRelativePath")

    verbose = true
    if (inMonorepo) {
        classpath({
            val riderModelClassPathFile: String by project
            File(riderModelClassPathFile).readLines()
        })
    } else {
        classpath({
            "${rdLibDirectory()}/rider-model.jar"
        })
    }
    sources("$modelDir/rider")

    hashFolder = "$buildDir"
    packages = "model.rider"

    generator {
        language = "kotlin"
        transform = "asis"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "com.jetbrains.rider.plugins.efcore.model"
        directory = "$ktOutput"
        if (inMonorepo) generatedFileSuffix = ".Pregenerated"
    }

    generator {
        language = "csharp"
        transform = "reversed"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "Rider.Plugins.EfCore"
        directory = "$csOutput"
        if (inMonorepo) generatedFileSuffix = ".Pregenerated"
    }
}

intellij {
    type.set("RD")
    version.set(productVersion)
    downloadSources.set(false)
    plugins.set(listOf(
        "com.intellij.database",
        "terminal"
    ))
}

tasks {
    wrapper {
        gradleVersion = "8.2.1"
        distributionType = Wrapper.DistributionType.ALL
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-${gradleVersion}-all.zip"
    }

    val riderSdkPath by lazy {
        val path = setupDependencies.get().idea.get().classes.resolve("lib/DotNetSdkForRdPlugins")
        if (!path.isDirectory) error("$path does not exist or not a directory")

        println("Rider SDK path: $path")
        return@lazy path
    }

    val prepareRiderBuildProps by registering {
        val generatedFile = project.buildDir.resolve("DotNetSdkPath.generated.props")

        inputs.property("dotNetSdkFile", { riderSdkPath })
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

    val rdgen by existing

    register("prepare") {
        dependsOn(rdgen, generateNuGetConfig, prepareRiderBuildProps)
    }

    val compileDotNet by registering {
        dependsOn(rdgen, generateNuGetConfig, prepareRiderBuildProps)
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
        dependsOn(rdgen)
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    patchPluginXml {
        sinceBuild.set("233.0")
        untilBuild.set("233.*")
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
            from("${buildDir}/distributions/${rootProject.name}-${version}.zip")
            into("${rootDir}/output")
        }
    }

    runIde {
        // For statistics:
        // jvmArgs("-Xmx1500m", "-Didea.is.internal=true", "-Dfus.internal.test.mode=true")
        jvmArgs("-Xmx1500m")
    }

    test {
        useTestNG()
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        environment["LOCAL_ENV_RUN"] = "true"
    }

    withType<org.jetbrains.intellij.tasks.PrepareSandboxTask> {
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
}

fun getProductMonorepoRoot(): File? {
    var currentDir = projectDir

    while (currentDir.parent != null) {
        if (currentDir.listFiles()?.any { it.name == ".dotnet-products.root.marker" } == true) {
            return currentDir
        }
        currentDir = currentDir.parentFile
    }

    return null
}