import com.jetbrains.rd.generator.gradle.RdGenTask

plugins {
    // Version is configured in gradle.properties
    id("com.jetbrains.rdgen")
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    maven("https://cache-redirector.jetbrains.com/maven-central")
    val rd_version: String by project
    if (rd_version == "SNAPSHOT") {
      mavenLocal()
    }
}

val isMonorepo = rootProject.projectDir != projectDir.parentFile
val efCoreRepoRoot: File = projectDir.parentFile

sourceSets {
    main {
        kotlin {
            srcDir(efCoreRepoRoot.resolve("protocol/src/main/kotlin/model"))
        }
    }
}

data class EfCoreGeneratorSettings(val csOutput: File, val ktOutput: File, val suffix: String)

val ktOutputRelativePath = "src/rider/generated/kotlin/com/jetbrains/rider/plugins/efcore/rd"
val efCoreGeneratorSettings = if (isMonorepo) {
    val monorepoRoot =
        buildscript.sourceFile?.parentFile?.parentFile?.parentFile?.parentFile?.parentFile ?: error("Cannot find products home")
    check(monorepoRoot.resolve(".ultimate.root.marker").isFile) {
        error("Incorrect location in monorepo: monorepoRoot='$monorepoRoot'")
    }
    val monorepoPreGeneratedRootDir = monorepoRoot.resolve("dotnet/Plugins/_RiderEfCore.Pregenerated")
    val monorepoPreGeneratedFrontendDir = monorepoPreGeneratedRootDir.resolve("Frontend")
    val monorepoPreGeneratedBackendDir = monorepoPreGeneratedRootDir.resolve("BackendModel")
    val ktOutputMonorepoRoot = monorepoPreGeneratedFrontendDir.resolve(ktOutputRelativePath)
    EfCoreGeneratorSettings(monorepoPreGeneratedBackendDir, ktOutputMonorepoRoot, ".Pregenerated")
} else {
    EfCoreGeneratorSettings(efCoreRepoRoot.resolve("src/dotnet/Rider.Plugins.EfCore/Rd"), efCoreRepoRoot.resolve(ktOutputRelativePath), ".Generated")
}

rdgen {
    verbose = true
    packages = "model"

    generator {
        language = "kotlin"
        transform = "asis"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "com.jetbrains.rider.plugins.efcore.model"
        directory = efCoreGeneratorSettings.ktOutput.absolutePath
        generatedFileSuffix = efCoreGeneratorSettings.suffix
    }

    generator {
        language = "csharp"
        transform = "reversed"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "Rider.Plugins.EfCore"
        directory = efCoreGeneratorSettings.csOutput.absolutePath
        generatedFileSuffix = efCoreGeneratorSettings.suffix
    }
}

tasks.withType<RdGenTask> {
    dependsOn(sourceSets["main"].runtimeClasspath)
    classpath(sourceSets["main"].runtimeClasspath)
}

dependencies {
    if (isMonorepo) {
        implementation(project(":rider-model"))
    } else {
        val rdVersion: String by project
        val rdKotlinVersion: String by project

        implementation("com.jetbrains.rd:rd-gen:$rdVersion")
        implementation("org.jetbrains.kotlin:kotlin-stdlib:$rdKotlinVersion")
        implementation(
            project(
                mapOf(
                    "path" to ":",
                    "configuration" to "riderModel"
                )
            )
        )
    }
}
