package me.seclerp.rider.plugins.efcore.state

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service
@State(name = "EfCoreCommonOptions", storages = [Storage("efCoreCommonOptions.xml")])
class CommonOptionsStateService : PersistentStateComponent<CommonOptionsState> {
    var myState = CommonOptionsState()

    override fun getState(): CommonOptionsState = myState

    override fun loadState(state: CommonOptionsState) {
        myState = state
    }

    fun clearPreferredProjects(prevMigrationsProject: String, prevStartupProject: String) {
        myState.migrationsToStartupProjects.remove(prevMigrationsProject)
        myState.startupToMigrationsProjects.remove(prevStartupProject)
    }

    fun setPreferredProjectsPair(migrationsProject: String, startupProject: String) {
        myState.migrationsToStartupProjects[migrationsProject] = startupProject
        myState.startupToMigrationsProjects[startupProject] = migrationsProject
    }

    fun getPreferredProjectPair(project: String): Pair<String, String>? {
        return if (myState.migrationsToStartupProjects.containsKey(project)) {
            val startupProject = myState.migrationsToStartupProjects[project]!!
            Pair(project, startupProject)
        } else if (myState.startupToMigrationsProjects.containsKey(project)) {
            val migrationsProject = myState.startupToMigrationsProjects[project]!!
            Pair(migrationsProject, project)
        } else {
            null
        }
    }

    companion object {
        fun getInstance(project: Project) = project.service<CommonOptionsStateService>()
    }
}