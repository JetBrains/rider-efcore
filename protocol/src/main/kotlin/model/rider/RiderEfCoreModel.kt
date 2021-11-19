package model.rider

import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*

@Suppress("unused")
object RiderEfCoreModel : Ext(SolutionModel.Solution) {
    init {
        call("getProjectNames", void, immutableList(string))
    }
}