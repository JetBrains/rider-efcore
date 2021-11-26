package me.seclerp.rider.plugins.efcore.commands

data class CliCommandResult(
    val command: String,
    val exitCode: Int,
    val output: String,
    val succeeded: Boolean,
    val error: String? = null
)
