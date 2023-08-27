package com.jetbrains.rider.plugins.efcore.features.shared.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.jetbrains.rd.ui.bedsl.extensions.valueOrEmpty
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.util.idea.runUnderProgress
import com.jetbrains.observables.*
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.CommonOptions
import com.jetbrains.rider.plugins.efcore.rd.*
import com.jetbrains.rider.plugins.efcore.settings.EfCoreUiSettingsStateService
import com.jetbrains.rider.plugins.efcore.state.DialogsStateService
import com.jetbrains.rider.plugins.efcore.ui.items.*

open class CommonDataContext(
    protected val intellijProject: Project,
    val requireDbContext: Boolean,
    val requireMigrationsInProject: Boolean,
) : DataContext() {
    private val beModel = intellijProject.solution.riderEfCoreModel
    protected val pluginSettings by lazy { EfCoreUiSettingsStateService.getInstance() }

    val availableStartupProjects = observableList<StartupProjectInfo>().withLogger("availableStartupProjects")
    val availableMigrationsProjects = observableList<MigrationsProjectInfo>().withLogger("availableMigrationsProjects")
    val availableDbContexts = observableList<DbContextInfo>().withLogger("availableDbContexts")
    val availableTargetFrameworks = observableList<String?>().withLogger("availableTargetFrameworks")
    val availableBuildConfigurations = observableList<String>().withLogger("availableBuildConfigurations")

    val migrationsProject = observable<MigrationsProjectInfo?>(null).withLogger("migrationsProject")
    val startupProject = observable<StartupProjectInfo?>(null).withLogger("startupProject")
    val dbContext = observable<DbContextInfo?>(null).withLogger("dbContext")
    val targetFramework = observable<String?>(null).withLogger("targetFramework")
    val buildConfiguration = observable<String?>(null).withLogger("buildConfiguration")
    val noBuild = observable(false).withLogger("noBuild")
    val additionalArguments = observable("").withLogger("additionalArguments")

    val migrationsProjectValidation: (MigrationsProjectInfo?) -> ValidationInfo? = {
        if (it == null)
            error(EfCoreUiBundle.message("dialog.message.you.should.selected.valid.migrations.project"))
        else null
    }

    val startupProjectValidation: (StartupProjectInfo?) -> ValidationInfo? = {
        if (it == null)
            error(EfCoreUiBundle.message("dialog.message.you.should.selected.valid.startup.project"))
        else
            null
    }

    val dbContextValidation: (DbContextInfo?) -> ValidationInfo? = {
        if (it == null || availableDbContexts.value.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.migrations.project.should.have.at.least.dbcontext"))
        else if (requireMigrationsInProject) {
            if (dbContext.value == null || migrationsProject.value == null)
                null
            else {
                val migrationsIdentity = MigrationsIdentity(
                    migrationsProject.value!!.id,
                    it.fullName)

                val hasMigrations = beModel.hasAvailableMigrations.runUnderProgress(
                    migrationsIdentity, intellijProject, EfCoreUiBundle.message("progress.title.checking.migrations"),
                    isCancelable = true,
                    throwFault = true
                )

                if (hasMigrations == null || !hasMigrations)
                    error(EfCoreUiBundle.message("dialog.message.selected.dbcontext.doesnt.have.migrations"))
                else null
            }
        } else null
    }

    val buildConfigurationValidation: (String?) -> ValidationInfo? = {
        if (it == null || availableBuildConfigurations.value.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.solution.doesnt.have.any.build.configurations"))
        else
            null
    }

    val targetFrameworkValidation: (String?) -> ValidationInfo? = {
        if (it == null || availableTargetFrameworks.value.isEmpty())
            error(EfCoreUiBundle.message("dialog.message.startup.project.should.have.at.least.supported.target.framework"))
        else
            null
    }

    override fun initBindings() {
        if (requireDbContext) {
            availableDbContexts.bindSafe(migrationsProject) {
                beModel.getAvailableDbContexts.runUnderProgress(
                    it.id, intellijProject, EfCoreUiBundle.message("progress.title.loading.dbcontext.classes"),
                    isCancelable = true,
                    throwFault = true
                )?.toMutableList() ?: mutableListOf()
            }
        }

        availableTargetFrameworks.bind(startupProject) {
            buildList {
                if (it != null) {
                    // null stands for <Default> case
                    add(null)
                    addAll(it.targetFrameworks)
                }
            }.toMutableList()
        }
    }

    override fun initData() {
        //
        // Startup projects
        availableStartupProjects.value = beModel.availableStartupProjects
            .valueOrEmpty()
            .toMutableList()

        startupProject.value = availableStartupProjects.value.firstOrNull()

        //
        // Migrations projects
        availableMigrationsProjects.value = beModel.availableMigrationProjects
            .valueOrEmpty()
            .toMutableList()

        migrationsProject.value = availableMigrationsProjects.value.firstOrNull()

        //
        // Build configurations
        availableBuildConfigurations.value = intellijProject.solution.solutionProperties.configurationsAndPlatformsCollection
            .valueOrEmpty()
            .map { it.configuration }
            .distinct() // To get around of different platforms for the same configurations
            .toMutableList()

        val currentBuildConfiguration = intellijProject.solution.solutionProperties.activeConfigurationPlatform.value
        val buildConfigurations = availableBuildConfigurations.value
        buildConfiguration.value =
            buildConfigurations.firstOrNull { it == currentBuildConfiguration?.configuration }
                ?: buildConfigurations.firstOrNull()

        //
        // DbContexts
        if (requireDbContext) {
            dbContext.value = availableDbContexts.firstOrNull()
        }

        //
        // Target frameworks
        targetFramework.value = availableTargetFrameworks.firstOrNull()
    }

    open fun loadState(commonDialogState: DialogsStateService.SpecificDialogState) {
        val migrationsProjectId = migrationsProject.value?.id
        val startupProjectId = startupProject.value?.id

        if (migrationsProjectId == null || startupProjectId == null) {
            return
        }

        if (requireDbContext) {
            val dbContextName = commonDialogState.get("${migrationsProjectId}:${KnownStateKeys.DB_CONTEXT}")
            availableDbContexts.value.firstOrNull { it.fullName == dbContextName }?.apply {
                dbContext.value = this
            }
        }

        val buildConfigurationName = commonDialogState.get(KnownStateKeys.BUILD_CONFIGURATION)
        availableBuildConfigurations.firstOrNull { it == buildConfigurationName }?.apply {
            buildConfiguration.value = this
        }

        val targetFrameworkName = commonDialogState.get("${startupProjectId}:${KnownStateKeys.TARGET_FRAMEWORK}")
        availableTargetFrameworks.value.firstOrNull { it == targetFrameworkName }?.apply {
            targetFramework.value = this
        }

        commonDialogState.getBool(KnownStateKeys.NO_BUILD)?.apply {
            noBuild.value = this
        }

        if (pluginSettings.storeSensitiveData) {
            commonDialogState.getSensitive(KnownStateKeys.ADDITIONAL_ARGUMENTS)?.apply {
                additionalArguments.value = this
            }
        }
    }

    open fun saveState(commonDialogState: DialogsStateService.SpecificDialogState) {
        val migrationsProjectId = migrationsProject.value?.id

        if (requireDbContext && dbContext.value != null) {
            commonDialogState.set("${migrationsProjectId}:${KnownStateKeys.DB_CONTEXT}", dbContext.value!!.fullName)
        }

        if (buildConfiguration.value != null) {
            commonDialogState.set(KnownStateKeys.BUILD_CONFIGURATION, buildConfiguration.value!!)
        }

        if (targetFramework.value != null) {
            commonDialogState.set(KnownStateKeys.TARGET_FRAMEWORK, targetFramework.value!!)
        }

        commonDialogState.set(KnownStateKeys.NO_BUILD, noBuild.value)

        if (pluginSettings.storeSensitiveData) {
            commonDialogState.setSensitive(KnownStateKeys.ADDITIONAL_ARGUMENTS, additionalArguments.value)
        }
    }

    override fun validate() = buildList {
        migrationsProjectValidation(migrationsProject.value)?.let { add(it) }
        startupProjectValidation(startupProject.value)?.let { add(it) }
        dbContextValidation(dbContext.value)?.let { add(it) }
        buildConfigurationValidation(buildConfiguration.value)?.let { add(it) }
        targetFrameworkValidation(targetFramework.value)?.let { add(it) }
    }

    protected fun getCommonOptions(): CommonOptions = CommonOptions(
        migrationsProject.value!!.fullPath,
        startupProject.value!!.fullPath,
        dbContext.value?.fullName,
        buildConfiguration.value!!,
        targetFramework.value,
        noBuild.value,
        additionalArguments.value
    )

    private object KnownStateKeys {
        const val DB_CONTEXT = "dbContext"
        const val BUILD_CONFIGURATION = "buildConfiguration"
        const val TARGET_FRAMEWORK = "targetFramework"
        const val NO_BUILD = "noBuild"
        const val ADDITIONAL_ARGUMENTS = "additionalArguments"
    }
}
