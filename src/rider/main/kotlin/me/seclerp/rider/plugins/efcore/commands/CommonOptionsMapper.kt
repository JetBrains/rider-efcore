package me.seclerp.rider.plugins.efcore.commands

class CommonOptionsMapper {
    fun getMappedTargetFramework(framework: String): String {
        when (framework) {
            ".NETCoreApp,Version=v3.1" -> return "netcoreapp3.1"
            ".NETStandard,Version=v2.1" -> return "netstandard2.1"
        }

        return framework
    }
}