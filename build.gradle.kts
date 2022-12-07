buildScript {
    repositories {
        maven { setUrl("https://cache-redirector.jetbrains.com/intellij-repository/snapshots") }
        maven { setUrl("https://cache-redirector.jetbrains.com/maven-central") }
    }

    // https://search.maven.org/artifact/com.jetbrains.rd/rd-gen
    dependencies {
        classpath("com.jetbrains.rd:rd-gen:2022.3.2")
    }
}

plugins {
    id("java")
    id("me.filippov.gradle.jvm.wrapper") version "0.14.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.9.0"
}

apply {
    plugin("com.jetbrains.rdgen")
}

val DotnetPluginId: String by project
val DotnetSolution: String by project
val RiderPluginId: String by project
val PluginVersion: String by project

val BuildConfiguration: String by project

val PublishToken: String by project
val PublishChannel: String by project

val RiderSdkVersion: String by project
val ProductVersion: String by project

val rdLibDirectory: () -> File = { file("${tasks.setupDependencies.get().idea.get().classes}/lib/rd") }
extra["rdLibDirectory"] = rdLibDirectory

version = PluginVersion

repositories {
    maven { setUrl("https://cache-redirector.jetbrains.com/intellij-repository/snapshots") }
    maven { setUrl("https://cache-redirector.jetbrains.com/maven-central") }
}

sourceSets {
    main {
        java.srcDir("src/rider/main/kotlin")
        resources.srcDir("src/rider/main/resources")
    }
}

apply(plugin = "com.jetbrains.rdgen")

configure<com.jetbrains.rd.generator.gradle.RdGenExtension> {
    val modelDir = file("$projectDir/protocol/src/main/kotlin/model")
    val csOutput = file("$projectDir/src/dotnet/$DotnetPluginId/Rd")
    val ktOutput = file("$projectDir/src/rider/main/kotlin/${RiderPluginId.replace(".","/").toLowerCase()}/rd")

    verbose = true
    classpath({
        "${rdLibDirectory()}/rider-model.jar"
    })
    sources("$modelDir/rider")
    hashFolder = "$rootDir/build/rdgen/rider"
    packages = "model.rider"

    generator {
        language = "kotlin"
        transform = "asis"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "me.seclerp.rider.plugins.efcore.model"
        directory = "$ktOutput"
    }

    generator {
        language = "csharp"
        transform = "reversed"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = RiderPluginId
        directory = "$csOutput"
    }
}

intellij {
    type.set("RD")
    version.set(RiderSdkVersion)
    downloadSources.set(false)
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
        distributionType = Wrapper.DistributionType.ALL
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-${gradleVersion}-all.zip"
    }

    val riderSdkPath by lazy {
        val path = setupDependencies.get().idea.get().classes.resolve("lib/DotNetSdkForRdPlugins")
        if (!path.isDirectory) error("$path does not exist or not a directory")

        println("Rider SDK path: $path")
        return@lazy path
    }
}