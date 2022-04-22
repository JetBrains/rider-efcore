package me.seclerp.rider.plugins.efcore.cli.api.models

data class DotnetEfVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun fromStrings(major: String, minor: String, patch: String): DotnetEfVersion {
            return DotnetEfVersion(major.toInt(), minor.toInt(), patch.toInt())
        }
    }
}
