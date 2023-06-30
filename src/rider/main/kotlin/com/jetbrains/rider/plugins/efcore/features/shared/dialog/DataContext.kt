package com.jetbrains.rider.plugins.efcore.features.shared.dialog

abstract class DataContext {
    open fun initBindings() {}
    open fun initData() {}
}