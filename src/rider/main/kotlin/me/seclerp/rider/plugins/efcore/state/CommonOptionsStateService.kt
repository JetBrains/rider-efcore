package me.seclerp.rider.plugins.efcore.state

import com.intellij.openapi.components.*
import java.util.*

@Service
@State(name = "EfCoreCommonOptions", storages = [Storage("efCoreCommonOptions.xml")])
class CommonOptionsStateService : PersistentStateComponent<CommonOptionsState> {
    private var myState = CommonOptionsState()

    override fun getState(): CommonOptionsState = myState

    override fun loadState(state: CommonOptionsState) {
        myState = state
    }

    fun clearPreferredProjectsPair(prevMigrationsProjectId: UUID, prevStartupProjectId: UUID) {
        myState.migrationsToStartupProjects.remove(prevMigrationsProjectId.toString())
        myState.startupToMigrationsProjects.remove(prevStartupProjectId.toString())
    }

    fun setPreferredProjectsPair(migrationsProjectId: UUID, startupProjectId: UUID) {
        val migrationsProjectString = migrationsProjectId.toString()
        val startupProjectString = startupProjectId.toString()

        myState.migrationsToStartupProjects[migrationsProjectString] = startupProjectString
        myState.startupToMigrationsProjects[startupProjectString] = migrationsProjectString
    }

    fun getPreferredProjectIdsPair(projectId: UUID): Pair<UUID, UUID>? {
        val projectIdString = projectId.toString()

        return if (myState.migrationsToStartupProjects.containsKey(projectIdString)) {
            val startupProject = myState.migrationsToStartupProjects[projectIdString]!!
            Pair(UUID.fromString(projectIdString), UUID.fromString(startupProject))
        } else if (myState.startupToMigrationsProjects.containsKey(projectIdString)) {
            val migrationsProject = myState.startupToMigrationsProjects[projectIdString]!!
            Pair(UUID.fromString(migrationsProject), UUID.fromString(projectIdString))
        } else null
    }

    fun getGlobalProjectIdsPair(): Pair<UUID, UUID>? {
        val migrationsProject = myState.solutionLevelOptions[MIGRATIONS_PROJECT_KEY] ?: return null
        val startupProject = myState.solutionLevelOptions[STARTUP_PROJECT_KEY] ?: return null

        return UUID.fromString(migrationsProject) to UUID.fromString(startupProject)
    }

    fun setGlobalProjectIdsPair(migrationsProjectId: UUID, startupProjectId: UUID) {
        myState.solutionLevelOptions[MIGRATIONS_PROJECT_KEY] = migrationsProjectId.toString()
        myState.solutionLevelOptions[STARTUP_PROJECT_KEY] = startupProjectId.toString()
    }

    companion object {
        private const val MIGRATIONS_PROJECT_KEY = "migrationsProject"
        private const val STARTUP_PROJECT_KEY = "startupProject"
    }
}