package com.jetbrains.rider.plugins.efcore.cases

import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.workspaceModel.ide.getInstance
import com.jetbrains.rdclient.util.idea.toIOFile
import com.jetbrains.rider.plugins.efcore.KnownTestData
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDataContext
import com.jetbrains.rider.plugins.efcore.framework.EfCoreDataContextTest
import com.jetbrains.rider.plugins.efcore.rd.MigrationsIdentity
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.projectView.workspace.getContentRootUrl
import com.jetbrains.rider.test.annotations.TestEnvironment
import com.jetbrains.rider.test.enums.PlatformType
import com.jetbrains.rider.test.env.enums.SdkVersion
import com.jetbrains.rider.test.framework.waitBackendAndWorkspaceModel
import com.jetbrains.rider.test.scriptingApi.changeFileSystem2
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.test.assertNotNull

@TestEnvironment(sdkVersion = SdkVersion.DOT_NET_6, platform = [PlatformType.ALL])
class AddMigrationContextTest : EfCoreDataContextTest() {
  @Suppress("HardCodedStringLiteral")
  @DataProvider
  fun validMigrationNames() = arrayOf(
      arrayOf("Abc", "Abc"),
      arrayOf("Abc123", "Abc123"),
      arrayOf("Abc_123", "Abc_123"),
      arrayOf("abc-123", "abc123"),
      arrayOf("Abc 123", "Abc123"),
      arrayOf("Abc$123", "Abc123"),
      arrayOf("123Abc", "_123Abc"),
  )

  @Test(dataProvider = "validMigrationNames")
  fun createMigrationWithName(sourceName: String, targetName: String) {
    installEfLocalTools(DotnetEfVersion(7, 0, 10))
    prepareContext { AddMigrationDataContext(project) }.withEfProjects("EFCoreConsoleApp", "EFCoreConsoleApp.DAL") {
      migrationName.value = sourceName
      assertValid()
      assert(executeCommand().succeeded)
      assertMigrationExist(targetName)
    }
  }

  override fun getSolutionDirectoryName() = KnownTestData.Solutions.EFCoreSolution.NAME

  private fun AddMigrationDataContext.assertMigrationExist(name: String) {
    val migrationsProject = assertNotNull(migrationsProject.value, "Migrations project is null")
    val dotnetProject = assertNotNull(findProjectById(migrationsProject.id), "Project wasn't found")
    val dotnetProjectDirectory = assertNotNull(dotnetProject.getContentRootUrl(VirtualFileUrlManager.getInstance(project))?.virtualFile, "Project directory wasn't found")

    changeFileSystem2(project) {
      arrayOf(dotnetProjectDirectory.toIOFile())
    }
    waitBackendAndWorkspaceModel(project)

    val model = project.solution.riderEfCoreModel
    val dbContext = assertNotNull(dbContext.value, "DbContext is null")
    val migrations = model.getAvailableMigrations.sync(MigrationsIdentity(migrationsProject.id, dbContext.fullName))
    val migration = migrations.firstOrNull { it.migrationShortName == name }
    assertNotNull(migration, "Migration doesn't exist")
  }
}

