package com.jetbrains.rider.plugins.efcore.cases

import com.jetbrains.rider.test.junit5.base.ApplicationTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("episode/dummy")
class DummyTest : ApplicationTestBase() {
  @Test
  fun someTest() {
    assert(true)
  }
}
