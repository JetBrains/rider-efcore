package me.seclerp.rider.plugins.efcore.state

import java.util.*

class CommonOptionsState {
//    var solutionLevelOptions: MutableMap<String, String> = mutableMapOf()
    var migrationsToStartupProjects: MutableMap<String, String> = mutableMapOf()
    var startupToMigrationsProjects: MutableMap<String, String> = mutableMapOf()
}
