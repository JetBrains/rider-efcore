package com.jetbrains.rider.plugins.efcore.cases

import com.jetbrains.rider.test.junit5.base.ApplicationTestBase
import com.jetbrains.rider.test.shared.constants.TeamCityTags
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag(TeamCityTags.Plugins.EFCore)
class DummyTest : ApplicationTestBase() {
  @Test
  fun someTest() {
    assert(true)
  }
}
