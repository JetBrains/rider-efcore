package com.jetbrains.rider.plugins.efcore.cases

import com.jetbrains.rider.test.base.BaseTestWithShell
import org.testng.annotations.Test

@Test
//@TestEnvironment(toolset = ToolsetVersion.TOOLSET_17_CORE, coreVersion = CoreVersion.DOT_NET_6)
class DummyTest : BaseTestWithShell() {
  @Test
  fun someTest() {
    assert(true)
  }
}
