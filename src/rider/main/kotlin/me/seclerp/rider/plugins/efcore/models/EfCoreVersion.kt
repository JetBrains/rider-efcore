package me.seclerp.rider.plugins.efcore.models

data class EfCoreVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun fromString(version: String): EfCoreVersion? {
            val parts = version.split(".").toTypedArray()
            if (parts.count() != 3) {
                return null
            }

            return EfCoreVersion(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}
