package com.jetbrains.rider.plugins.efcore.cases

import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDataContext
import com.jetbrains.rider.plugins.efcore.framework.EfCoreDataContextTest
import com.jetbrains.rider.test.annotations.TestEnvironment
import com.jetbrains.rider.test.enums.PlatformType
import com.jetbrains.rider.test.env.enums.SdkVersion
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@TestEnvironment(sdkVersion = SdkVersion.DOT_NET_6, platform = [PlatformType.ALL])
class AddMigrationTest : EfCoreDataContextTest() {
  @Suppress("HardCodedStringLiteral")
  @DataProvider
  fun validMigrationNames() = arrayOf(
      "Abc",
      "Abc123",
      "Abc_123",
      "Abc-123",
  )

  @Test(dataProvider = "validMigrationNames")
  fun createMigrationWithName(name: String) {
    installEfLocalTools(DotnetEfVersion(7, 0, 10))
    prepareContext { AddMigrationDataContext(project) }.withEfProjects("EFCoreConsoleApp", "EFCoreConsoleApp.DAL") {
      migrationName.value = name
      assertValid()
      assert(executeCommand().succeeded)
    }
  }

  override fun getSolutionDirectoryName() = "EFCoreSolution"
}