package me.seclerp.rider.plugins.efcore.cli.execution

data class CliCommandResult(
    val command: String,
    val exitCode: Int,
    val output: String,
    val succeeded: Boolean,
    val error: String? = null
)
