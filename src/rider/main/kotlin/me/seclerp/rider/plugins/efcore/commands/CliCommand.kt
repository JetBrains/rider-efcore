package me.seclerp.rider.plugins.efcore.commands

import java.io.IOException
import java.util.concurrent.TimeUnit

class CliCommand(private val fullCommand: String) {
    fun execute(): CliCommandResult {
        return try {
            val runtime = Runtime.getRuntime()
            val proc = runtime.exec(fullCommand)

            proc.waitFor(60, TimeUnit.MINUTES)

            val output = proc.inputStream.bufferedReader().readText()
            val error = proc.errorStream.bufferedReader().readText()
            val exitCode = proc.exitValue()

            CliCommandResult(fullCommand, exitCode, output, exitCode == 0, error)
        } catch(e: IOException) {
            e.printStackTrace()

            CliCommandResult(fullCommand, -1, e.toString(), false)
        }
    }
}