package me.seclerp.rider.plugins.efcore.cli.api.models

data class DotnetEfVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun fromString(version: String): DotnetEfVersion? {
            val parts = version.split(".").toTypedArray()
            if (parts.count() != 3) {
                return null
            }

            return DotnetEfVersion(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}
