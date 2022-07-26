package me.seclerp.rider.plugins.efcore.features.shared.services

import com.intellij.openapi.project.Project
import me.seclerp.rider.plugins.efcore.state.CommonOptionsStateService
import me.seclerp.rider.plugins.efcore.state.DbScaffoldOptionsStateService
import me.seclerp.rider.plugins.efcore.ui.items.MigrationsProjectItem
import me.seclerp.rider.plugins.efcore.ui.items.StartupProjectItem
import java.util.*

class PreferredProjectsManager(
    intellijProject: Project
) {
    private val commonOptionsStateService = CommonOptionsStateService.getInstance(intellijProject)
    private val dbScaffoldOptionsStateService = DbScaffoldOptionsStateService.getInstance(intellijProject)
    private var prevPreferredMigrationsProjectId: UUID? = null
    private var prevPreferredStartupProjectId: UUID? = null

    fun getProjectPair(preferredProjectId: UUID?, migrationsProjects: Array<MigrationsProjectItem>,
                       startupProjects: Array<StartupProjectItem>): Pair<MigrationsProjectItem?, StartupProjectItem?> {

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
                migrationsProjects.find { it.data.id == migrationsProjectId } ?: migrationsProjects.firstOrNull()

            val startupProject =
                startupProjects.find { it.data.id == startupProjectId } ?: startupProjects.firstOrNull()

            return Pair(migrationsProject, startupProject)
        }

        return getDefaultProjects(preferredProjectId, migrationsProjects, startupProjects)
    }

    fun setProjectPair(migrationsProjectItem: MigrationsProjectItem, startupProjectItem: StartupProjectItem) {
        if (prevPreferredMigrationsProjectId != null && prevPreferredStartupProjectId != null)
            commonOptionsStateService.clearPreferredProjectsPair(
                prevPreferredMigrationsProjectId!!,
                prevPreferredStartupProjectId!!
            )

        commonOptionsStateService.setPreferredProjectsPair(migrationsProjectItem.data.id, startupProjectItem.data.id)
    }

    fun getGlobalProjectPair(migrationsProjects: Array<MigrationsProjectItem>,
                             startupProjects: Array<StartupProjectItem>
    ): Pair<MigrationsProjectItem?, StartupProjectItem?> {
        val ids = commonOptionsStateService.getGlobalProjectIdsPair()

        return if (ids == null) {
            migrationsProjects.firstOrNull() to startupProjects.firstOrNull()
        } else {
            val migrationProject = migrationsProjects.find { it.data.id == ids.first }
                ?: migrationsProjects.firstOrNull()
            val startupProject = startupProjects.find { it.data.id == ids.second }
                ?: startupProjects.firstOrNull()
            migrationProject to startupProject
        }
    }

    fun setGlobalProjectPair(migrationsProjectItem: MigrationsProjectItem, startupProjectItem: StartupProjectItem) {
        commonOptionsStateService.setGlobalProjectIdsPair(migrationsProjectItem.data.id, startupProjectItem.data.id)
    }

     fun getScaffoldString(fieldName: String): String{
        return dbScaffoldOptionsStateService.getOptionString(fieldName)
    }

    fun setScaffoldString(fieldName: String, value: String){
        dbScaffoldOptionsStateService.setOptionString(fieldName, value)
    }

    fun getScaffoldBoolean(fieldName: String): Boolean{
        return dbScaffoldOptionsStateService.getOptionBoolean(fieldName)
    }

    fun setScaffoldBoolean(fieldName: String, value: Boolean){
        dbScaffoldOptionsStateService.setOptionBoolean(fieldName, value)
    }

    private fun getDefaultProjects(preferredProjectId: UUID?, migrationsProjects: Array<MigrationsProjectItem>,
                                   startupProjects: Array<StartupProjectItem>): Pair<MigrationsProjectItem?, StartupProjectItem?> {
        val migrationsProject =
            migrationsProjects.find { it.data.id == preferredProjectId }
                ?: migrationsProjects.firstOrNull()

        val startupProject =
            startupProjects.find { it.data.id == preferredProjectId }
                ?: startupProjects.firstOrNull()

        return Pair(migrationsProject, startupProject)
    }
}