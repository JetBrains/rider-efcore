package com.jetbrains.rider.plugins.efcore.features.database.drop

import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.efcore.features.shared.dialog.CommonDataContext

class DropDatabaseDataContext(intellijProject: Project, requireDbContext: Boolean) : CommonDataContext(intellijProject, requireDbContext)