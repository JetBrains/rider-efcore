package com.jetbrains.rider.plugins.efcore.v2.toolset

import org.jetbrains.annotations.NonNls

data class EfCoreVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        @NonNls
        val SEMVER_REGEX =
            Regex("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([\\dA-Za-z-]+(?:\\.[\\dA-Za-z-]+)*))?(?:\\+[\\dA-Za-z-]+)?")

        fun parse(version: String): EfCoreVersion? {
            val match = SEMVER_REGEX.find(version) ?: return null

            if (match.groups.size < 3) {
                return null
            }

            return fromStrings(match.groups[1]!!.value, match.groups[2]!!.value, match.groups[3]!!.value)
        }

        private fun fromStrings(major: String, minor: String, patch: String): EfCoreVersion {
            return EfCoreVersion(major.toInt(), minor.toInt(), patch.toInt())
        }
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}
