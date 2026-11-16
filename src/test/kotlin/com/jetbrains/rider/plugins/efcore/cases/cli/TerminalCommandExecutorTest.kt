package com.jetbrains.rider.plugins.efcore.cases.cli

import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.TerminalExecutionConsole
import com.jetbrains.rdclient.util.idea.pumpMessages
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommand
import com.jetbrains.rider.plugins.efcore.cli.execution.CliCommandPresentationInfo
import com.jetbrains.rider.plugins.efcore.cli.execution.TerminalCommandExecutor
import com.jetbrains.rider.shared.run.FormatPreservingPtyCommandLine
import com.jetbrains.rider.test.annotations.Solution
import com.jetbrains.rider.test.annotations.TestEnvironment
import com.jetbrains.rider.test.annotations.TestSettings
import com.jetbrains.rider.test.annotations.report.Issue
import com.jetbrains.rider.test.base.PerTestSolutionTestBase
import com.jetbrains.rider.test.enums.BuildTool
import com.jetbrains.rider.test.enums.PlatformType
import com.jetbrains.rider.test.enums.sdk.SdkVersion
import com.jetbrains.rider.test.scriptingApi.runBlockingWithFlushing
import com.jetbrains.rider.test.shared.constants.TeamCityTags
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

/**
 * Runs a command through [TerminalCommandExecutor], as the EF Core actions do, and reads what the EF Core tab shows.
 *
 * The command writes colored text to its stderr, as dotnet-ef 11 does. The script writes one escape sequence in two parts, so the
 * parts arrive in two chunks of output.
 */
@Tag(TeamCityTags.Plugins.EFCore)
@TestEnvironment(platform = [PlatformType.MAC_OS_ALL, PlatformType.LINUX_ALL])
@TestSettings(buildTool = BuildTool.SDK, sdkVersion = SdkVersion.LATEST_STABLE)
@Solution("TerminalCommandExecutorTest")
class TerminalCommandExecutorTest : PerTestSolutionTestBase() {
  @Test
  @Issue("RIDER-145369")
  fun stderrReachesTheTabInTheStreamOfStdout() {
    val script = createTempFile("terminal-command-executor", ".sh")
    try {
      script.writeText(SCRIPT)
      val commandLine = FormatPreservingPtyCommandLine().withExePath("/bin/sh").withParameters(script.pathString)
      val command = CliCommand("", commandLine, CliCommandPresentationInfo(TAB_NAME, TAB_NAME))

      val result = runBlockingWithFlushing("TerminalCommandExecutor.execute", 60.seconds) {
        TerminalCommandExecutor(project).execute(command)
      }

      assertNotNull(result)
      assertEquals(0, result!!.exitCode)
      assertTrue(result.output.contains(STDERR_TEXT), "The output has the stderr text: ${result.output}")
      assertEquals("", result.error)

      val console = findTabConsole()
      pumpMessages(Duration.ofSeconds(30)) { console.terminalWidget.text.contains(LAST_LINE) }
      val text = console.terminalWidget.text
      assertTrue(text.contains(STDOUT_TEXT), "The tab has the stdout text: $text")
      assertTrue(text.contains(STDERR_TEXT), "The tab has the stderr text: $text")
      assertTrue(text.contains(LAST_LINE), "The tab has the last line: $text")
      assertFalse(text.contains("0m$STDERR_TEXT"), "The tab has a part of an escape sequence: $text")
    }
    finally {
      script.deleteIfExists()
    }
  }

  private fun findTabConsole(): TerminalExecutionConsole {
    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(EfCoreUiBundle.message("tab.task.ef.core"))
    assertNotNull(toolWindow, "The EF Core tool window is registered")
    val content = toolWindow!!.contentManager.contents.last { it.displayName == TAB_NAME }
    return content.disposer as TerminalExecutionConsole
  }

  companion object {
    private const val TAB_NAME = "Terminal Command Executor Test"
    private const val STDOUT_TEXT = "text-on-stdout"
    private const val STDERR_TEXT = "text-on-stderr"
    private const val LAST_LINE = "last-line"

    // The pause makes the two parts of ESC[30m arrive in separate chunks.
    private val SCRIPT = """
      printf '$STDOUT_TEXT\n'
      printf '\033[3' >&2
      sleep 0.5
      printf '0m$STDERR_TEXT\033[39;49m\n' >&2
      printf '$LAST_LINE\n'
    """.trimIndent()
  }
}
