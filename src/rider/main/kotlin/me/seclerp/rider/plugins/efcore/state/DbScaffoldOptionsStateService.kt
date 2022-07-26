package me.seclerp.rider.plugins.efcore.state

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service
@State(name = "EfCoreDbScaffoldOptions", storages = [Storage("efCoreDbScaffoldOptions.xml")])
class DbScaffoldOptionsStateService: PersistentStateComponent<DbScaffoldOptionsState> {
    private var myState = DbScaffoldOptionsState()

    override fun getState(): DbScaffoldOptionsState = myState

    override fun loadState(state: DbScaffoldOptionsState) {
        myState = state
    }

    fun setOptionString(fieldName: String, value: String) {
        myState.solutionLevelOptions[fieldName] = value
    }

    fun setOptionBoolean(fieldName: String, value: Boolean){
        myState.solutionLevelOptions[fieldName] = value.toString()
    }

    fun getOptionString(fieldName: String): String {

        return myState.solutionLevelOptions[fieldName] ?: return ""
    }

    fun getOptionBoolean(fieldName: String): Boolean {

        return myState.solutionLevelOptions[fieldName].toBoolean()
    }

    companion object {
        fun getInstance(project: Project) = project.service<DbScaffoldOptionsStateService>()

    }
} 