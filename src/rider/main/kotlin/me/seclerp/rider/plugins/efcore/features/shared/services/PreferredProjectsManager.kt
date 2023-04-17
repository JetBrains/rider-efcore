package me.seclerp.rider.plugins.efcore.features.shared.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.rd.MigrationsProjectInfo
import me.seclerp.rider.plugins.efcore.rd.StartupProjectInfo
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import java.util.*

@Service(Service.Level.PROJECT)
class PreferredProjectsManager(
    intellijProject: Project
) {
    private val commonOptionsStateService = intellijProject.service<CommonOptionsStateService>()
    private var prevPreferredMigrationsProjectId: UUID? = null
    private var prevPreferredStartupProjectId: UUID? = null

    fun getProjectPair(preferredProjectId: UUID?, migrationsProjects: Collection<MigrationsProjectInfo>,
                       startupProjects: Collection<StartupProjectInfo>): Pair<MigrationsProjectInfo?, StartupProjectInfo?> {

        if (preferredProjectId == null) {
            return getDefaultProjects(null, migrationsProjects, startupProjects)
        }

        val preferredProjects =
            commonOptionsStateService.getPreferredProjectIdsPair(preferredProjectId)

        if (preferredProjects != null) {
            val (migrationsProjectId, startupProjectId) = preferredProjects
            prevPreferredMigrationsProjectId = migrationsProjectId
            prevPreferredStartupProjectId = startupProjectId

            val migrationsProject =
                migrationsProjects.find { it.id == migrationsProjectId } ?: migrationsProjects.firstOrNull()

            val startupProject =
                startupProjects.find { it.id == startupProjectId } ?: startupProjects.firstOrNull()

            return Pair(migrationsProject, startupProject)
        }

        return getDefaultProjects(preferredProjectId, migrationsProjects, startupProjects)
    }

    fun setProjectPair(migrationsProjectInfo: MigrationsProjectInfo, startupProjectInfo: StartupProjectInfo) {
        if (prevPreferredMigrationsProjectId != null && prevPreferredStartupProjectId != null)
            commonOptionsStateService.clearPreferredProjectsPair(
                prevPreferredMigrationsProjectId!!,
                prevPreferredStartupProjectId!!
            )

        commonOptionsStateService.setPreferredProjectsPair(migrationsProjectInfo.id, startupProjectInfo.id)
    }

    fun getGlobalProjectPair(migrationsProjects: Collection<MigrationsProjectInfo>,
                             startupProjects: Collection<StartupProjectInfo>
    ): Pair<MigrationsProjectInfo?, StartupProjectInfo?> {
        val ids = commonOptionsStateService.getGlobalProjectIdsPair()

        return if (ids == null) {
            migrationsProjects.firstOrNull() to startupProjects.firstOrNull()
        } else {
            val migrationProject = migrationsProjects.find { it.id == ids.first }
                ?: migrationsProjects.firstOrNull()
            val startupProject = startupProjects.find { it.id == ids.second }
                ?: startupProjects.firstOrNull()
            migrationProject to startupProject
        }
    }

    fun setGlobalProjectPair(migrationsProjectInfo: MigrationsProjectInfo, startupProjectInfo: StartupProjectInfo) {
        commonOptionsStateService.setGlobalProjectIdsPair(migrationsProjectInfo.id, startupProjectInfo.id)
    }

    private fun getDefaultProjects(preferredProjectId: UUID?, migrationsProjects: Collection<MigrationsProjectInfo>,
                                   startupProjects: Collection<StartupProjectInfo>): Pair<MigrationsProjectInfo?, StartupProjectInfo?> {
        val migrationsProject =
            migrationsProjects.find { it.id == preferredProjectId }
                ?: migrationsProjects.firstOrNull()

        val startupProject =
            startupProjects.find { it.id == preferredProjectId }
                ?: startupProjects.firstOrNull()

        return Pair(migrationsProject, startupProject)
    }
}