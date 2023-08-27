package com.jetbrains.rider.plugins.efcore.features.shared.dialog

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.ui.ValidationInfo

abstract class DataContext {
    open fun initBindings() {}
    open fun initData() {}
    open fun validate(): List<ValidationInfo> = emptyList()
    open fun generateCommand(): GeneralCommandLine = GeneralCommandLine()
}