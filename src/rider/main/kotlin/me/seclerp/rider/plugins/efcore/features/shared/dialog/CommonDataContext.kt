package me.seclerp.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.project.Project
import com.jetbrains.rd.ui.bedsl.extensions.valueOrEmpty
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import me.seclerp.observables.ObservableProperty
import me.seclerp.rider.plugins.efcore.rd.DbContextInfo
import me.seclerp.rider.plugins.efcore.rd.MigrationsProjectInfo
import me.seclerp.rider.plugins.efcore.rd.RiderEfCoreModel
import me.seclerp.rider.plugins.efcore.rd.StartupProjectInfo
import me.seclerp.rider.plugins.efcore.state.DialogsStateService

class CommonDataContext(
    intellijProject: Project,
    beModel: RiderEfCoreModel,
    private val requireDbContext: Boolean
) {
    val availableStartupProjects = beModel.availableStartupProjects.valueOrEmpty()
    val availableMigrationsProjects = beModel.availableMigrationProjects.valueOrEmpty()
    val availableBuildConfigurations =
        intellijProject.solution.solutionProperties.configurationsAndPlatformsCollection
            .valueOrEmpty()
            .map { it.configuration }
            .distinct() // To get around of different platforms for the same configurations
            .toTypedArray()
    val availableDbContexts = ObservableProperty<List<DbContextInfo>>(listOf())
    val availableTargetFrameworks = ObservableProperty<List<String?>>(listOf())

    val migrationsProject = ObservableProperty<MigrationsProjectInfo>(null)
    val startupProject = ObservableProperty<StartupProjectInfo>(null)
    val dbContext = ObservableProperty<DbContextInfo>(null) // Depends on Migrations project
    val buildConfiguration = ObservableProperty<String>(null)
    val targetFramework = ObservableProperty<String>(null) // Depends on Startup project
    val noBuild = ObservableProperty(false)
    val additionalArguments = ObservableProperty("")

    init {
        startupProject.afterChange {
            if (it != null) {
                availableTargetFrameworks.value = buildList {
                    // null stands for <Default> case
                    add(null)
                    addAll(it.targetFrameworks)
                }
            }
        }

        if (requireDbContext) {
            migrationsProject.afterChange {
                val dbContexts = beModel.getAvailableDbContexts.runUnderProgress(
                    it!!.name, intellijProject, "Loading DbContext classes...",
                    isCancelable = true,
                    throwFault = true
                )

                if (dbContexts != null) {
                    availableDbContexts.value = dbContexts
                }
            }
        }

        val currentBuildConfiguration = intellijProject.solution.solutionProperties.activeConfigurationPlatform.value

        buildConfiguration.value =
            availableBuildConfigurations.find {
                it == currentBuildConfiguration?.configuration
            } ?: availableBuildConfigurations.firstOrNull()
    }

    fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
        val migrationsProjectId = migrationsProject.notNullValue.id
        val startupProjectId = startupProject.notNullValue.id

        if (requireDbContext) {
            val dbContextName = commonDialogState.get("${migrationsProjectId}:${KnownStateKeys.DB_CONTEXT}")
            val dbContext = availableDbContexts.notNullValue.firstOrNull { it.fullName == dbContextName }
            if (dbContext != null) {
                this.dbContext.value = dbContext
            }
        }

        val buildConfigurationName = commonDialogState.get(KnownStateKeys.BUILD_CONFIGURATION)
        val buildConfiguration = availableBuildConfigurations.firstOrNull { it == buildConfigurationName }
        if (buildConfiguration != null) {
            this.buildConfiguration.value = buildConfiguration
        }

        val targetFrameworkName = commonDialogState.get("${startupProjectId}:${KnownStateKeys.TARGET_FRAMEWORK}")
        val targetFramework = availableTargetFrameworks.notNullValue.firstOrNull { it == targetFrameworkName }
        if (targetFramework != null) {
            this.targetFramework.value = targetFramework
        }

        val noBuild = commonDialogState.getBool(KnownStateKeys.NO_BUILD) ?: false
        this.noBuild.value = noBuild

        val additionalArguments = commonDialogState.get(KnownStateKeys.ADDITIONAL_ARGUMENTS) ?: ""
        this.additionalArguments.value = additionalArguments
    }

    fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        if (requireDbContext && dbContext.value != null) {
            commonDialogState.set(KnownStateKeys.DB_CONTEXT, dbContext.notNullValue.fullName)
        }

        if (buildConfiguration.value != null) {
            commonDialogState.set(KnownStateKeys.BUILD_CONFIGURATION, buildConfiguration.notNullValue)
        }

        if (targetFramework.value != null) {
            commonDialogState.set(KnownStateKeys.TARGET_FRAMEWORK, targetFramework.notNullValue)
        }

        commonDialogState.set(KnownStateKeys.NO_BUILD, noBuild.value.toString())
        commonDialogState.set(KnownStateKeys.ADDITIONAL_ARGUMENTS, additionalArguments.value.toString())
    }

    private object KnownStateKeys {
        val DB_CONTEXT = "dbContext"
        val BUILD_CONFIGURATION = "buildConfiguration"
        val TARGET_FRAMEWORK = "targetFramework"
        val NO_BUILD = "noBuild"
        val ADDITIONAL_ARGUMENTS = "additionalArguments"
    }
}
