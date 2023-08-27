package com.jetbrains.rider.plugins.efcore.cases

import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDataContext
import com.jetbrains.rider.plugins.efcore.framework.EfCoreDataContextTest
import com.jetbrains.rider.test.annotations.TestEnvironment
import com.jetbrains.rider.test.enums.PlatformType
import com.jetbrains.rider.test.env.enums.SdkVersion
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@TestEnvironment(sdkVersion = SdkVersion.DOT_NET_7, platform = [PlatformType.ALL])
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
  fun `Create a new migration with a valid name when the migration list is empty, should create migration`(name: String) {
    prepareContext { AddMigrationDataContext(project) }.withEfProjects("EFCoreConsoleApp", "EFCoreConsoleApp.DAL") {
      migrationName.value = name
      assertValid()
      val command = generateCommand()
    }
  }

  override fun getSolutionDirectoryName() = "EFCoreSolution"
}