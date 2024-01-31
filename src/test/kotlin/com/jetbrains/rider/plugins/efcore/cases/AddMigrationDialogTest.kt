@file:Suppress("HardCodedStringLiteral")

package com.jetbrains.rider.plugins.efcore.cases

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.jetbrains.rdclient.util.idea.toIOFile
import com.jetbrains.rider.plugins.efcore.KnownTestData
import com.jetbrains.rider.plugins.efcore.cli.api.models.DotnetEfVersion
import com.jetbrains.rider.plugins.efcore.features.migrations.add.AddMigrationDialogWrapper
import com.jetbrains.rider.plugins.efcore.framework.EfCoreDialogTest
import com.jetbrains.rider.plugins.efcore.rd.MigrationsIdentity
import com.jetbrains.rider.plugins.efcore.rd.riderEfCoreModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.projectView.workspace.getContentRootUrl
import com.jetbrains.rider.test.annotations.TestEnvironment
import com.jetbrains.rider.test.env.enums.BuildTool
import com.jetbrains.rider.test.env.enums.SdkVersion
import com.jetbrains.rider.test.framework.waitBackendAndWorkspaceModel
import com.jetbrains.rider.test.scriptingApi.changeFileSystem2
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.test.assertNotNull

@TestEnvironment(sdkVersion = SdkVersion.AUTODETECT, buildTool = BuildTool.AUTODETECT)
class AddMigrationDialogTest : EfCoreDialogTest() {
    private val efToolset = DotnetEfVersion(7, 0, 10)

    override fun getSolutionDirectoryName() = KnownTestData.Solutions.EFCoreSolution.NAME

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
    suspend fun createMigrationUiTest(sourceName: String, targetName: String) {
        installEfLocalTools(efToolset)
        withDialog({ AddMigrationDialogWrapper(efToolset, project, null) }) { dialog, dataCtx ->
            selectMigrationsProject(KnownTestData.Solutions.EFCoreSolution.EfCoreConsoleApp_DAL)
            selectStartupProject(KnownTestData.Solutions.EFCoreSolution.EfCoreConsoleApp)
            setMigrationName(sourceName)

            executeCommand()

            assertMigrationExist(targetName)
        }
    }

    private fun AddMigrationDialogWrapper.setMigrationName(name: String) {
        assertNotNull(migrationNameComponent, "Migration name field wasn't initialized") {
            it.text = name
        }
    }

    private fun AddMigrationDialogWrapper.assertMigrationExist(name: String) {
        val migrationsProject = assertNotNull(dataCtx.migrationsProject.value, "Migrations project is null")
        val dotnetProject = assertNotNull(findProjectById(migrationsProject.id), "Project wasn't found")
        val dotnetProjectDirectory = assertNotNull(dotnetProject.getContentRootUrl(WorkspaceModel.getInstance(project).getVirtualFileUrlManager())?.virtualFile, "Project directory wasn't found")

        changeFileSystem2(project) {
            arrayOf(dotnetProjectDirectory.toIOFile())
        }
        waitBackendAndWorkspaceModel(project)

        val model = project.solution.riderEfCoreModel
        val dbContext = assertNotNull(dataCtx.dbContext.value, "DbContext is null")
        val migrations = model.getAvailableMigrations.sync(MigrationsIdentity(migrationsProject.id, dbContext.fullName))
        val migration = migrations.firstOrNull { it.migrationShortName == name }
        assertNotNull(migration, "Migration doesn't exist")
    }
}