@file:Suppress("HardCodedStringLiteral")

package com.jetbrains.rider.plugins.efcore.cases

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.jetbrains.rdclient.util.idea.toIOFile
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
import com.jetbrains.rider.test.framework.waitBackendAndWorkspaceModelSuspending
import com.jetbrains.rider.test.framework.waitBackendSuspending
import com.jetbrains.rider.test.scriptingApi.changeFileSystem2
import com.jetbrains.rider.test.scriptingApi.runBlockingWithFlushing
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration

@TestEnvironment(sdkVersion = SdkVersion.AUTODETECT, buildTool = BuildTool.AUTODETECT)
class AddMigrationDialogTest : EfCoreDialogTest() {
    private val efToolset = DotnetEfVersion(7, 0, 10)

    override fun getSolutionDirectoryName() = "EFCoreSolution"

    @Suppress("HardCodedStringLiteral")
    @DataProvider
    fun validMigrationNames() = arrayOf(
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Abc", "Abc" ),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Abc123", "Abc123",),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Abc_123", "Abc_123"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "abc-123", "abc123"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Abc 123", "Abc123"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Abc$123", "Abc123"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "123Abc", "_123Abc"),
    )

    @Suppress("HardCodedStringLiteral")
    @DataProvider
    fun validMigrationFolders() = arrayOf(
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Migrations"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "_Migrations"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Migrations 123"),
        arrayOf("EFCoreConsoleApp", "EFCoreConsoleApp.DAL", "BloggingContext", "Migrations$123"),
    )

    @Test(dataProvider = "validMigrationNames")
    fun addMigrationCustomName(startupProjectName: String, migrationsProjectName: String, dbContextName: String, sourceName: String, targetName: String) {
        runBlockingWithFlushing("addMigrationCustomName", Duration.INFINITE) {
            installEfLocalTools(efToolset)
            waitBackendSuspending(project)
            withDialog({ AddMigrationDialogWrapper(efToolset, project, null) }) { dialog, dataCtx ->
                selectStartupProject(startupProjectName)
                selectMigrationsProject(migrationsProjectName)
                selectDbContext(dbContextName)
                setMigrationName(sourceName)

                executeCommand()

                assertMigrationExist(targetName)
            }
        }
    }

    @Test(dataProvider = "validMigrationFolders")
    fun addMigrationCustomFolder(startupProjectName: String, migrationsProjectName: String, dbContextName: String, folderName: String) {
        runBlockingWithFlushing("addMigrationCustomFolder", Duration.INFINITE) {
            installEfLocalTools(efToolset)
            waitBackendSuspending(project)
            withDialog({ AddMigrationDialogWrapper(efToolset, project, null) }) { dialog, dataCtx ->
                selectStartupProject(startupProjectName)
                selectMigrationsProject(migrationsProjectName)
                selectDbContext(dbContextName)
                val migrationName = "SomeSafeName"
                setMigrationName(migrationName)
                setMigrationFolder(folderName)

                executeCommand()

                assertMigrationExist(migrationName)
            }
        }
    }

    private fun AddMigrationDialogWrapper.setMigrationName(name: String) {
        assertNotNull(migrationNameComponent, "Migration name field wasn't initialized") {
            it.text = name
        }
    }

    private fun AddMigrationDialogWrapper.setMigrationFolder(name: String) {
        assertNotNull(migrationsFolderComponent, "Migration folder field wasn't initialized") {
            it.text = name
        }
    }

    private suspend fun AddMigrationDialogWrapper.assertMigrationExist(name: String) {
        val migrationsProject = assertNotNull(dataCtx.migrationsProject.value, "Migrations project is null")
        val dotnetProject = assertNotNull(findProjectById(migrationsProject.id), "Project wasn't found")
        val dotnetProjectDirectory = assertNotNull(dotnetProject.getContentRootUrl(WorkspaceModel.getInstance(project).getVirtualFileUrlManager())?.virtualFile, "Project directory wasn't found")

        changeFileSystem2(project) {
            arrayOf(dotnetProjectDirectory.toIOFile())
        }
        waitBackendAndWorkspaceModelSuspending(project)

        val model = project.solution.riderEfCoreModel
        val dbContext = assertNotNull(dataCtx.dbContext.value, "DbContext is null")
        val migrations = model.getAvailableMigrations.startSuspending(MigrationsIdentity(migrationsProject.id, dbContext.fullName))
        val migration = migrations.firstOrNull { it.migrationShortName == name }
        assertNotNull(migration, "Migration can't be found on backend")

        val migrationsFolder =  assertNotNull(dataCtx.migrationsOutputFolder.value, "Migrations folder is null")
        val migrationsFolderFullPath = File(migrationsProject.fullPath).parentFile.resolve(migrationsFolder)
        val migrationFullPath = migrationsFolderFullPath.resolve("${migration.migrationLongName}.cs")
        assertTrue(migrationFullPath.exists(), "Migration can't be found at path '$migrationFullPath'")
    }
}