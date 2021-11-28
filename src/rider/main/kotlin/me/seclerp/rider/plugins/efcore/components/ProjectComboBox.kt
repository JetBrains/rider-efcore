package me.seclerp.rider.plugins.efcore.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.Cell
import com.intellij.ui.layout.CellBuilder
import com.jetbrains.rd.ide.model.ProjectInfo
import java.awt.event.ItemEvent
import javax.swing.ComboBoxModel

fun Cell.projectComboBox(model: ComboBoxModel<ProjectInfo>,
                         getter: () -> ProjectInfo?,
                         setter: (ProjectInfo?) -> Unit): CellBuilder<ComboBox<ProjectInfo>> {
    val projectComboBoxBuilder =
        comboBox(model,
            getter,
            setter,
            ProjectComboBoxRendererAdapter())
            .constraints(CCFlags.pushX, CCFlags.growX)

    // Setter provided above called only on submit, so we need additional change detection
    projectComboBoxBuilder.component.addItemListener {
        if (it.stateChange == ItemEvent.SELECTED) {
            setter(it.item as ProjectInfo)
        }
    }

    return projectComboBoxBuilder
}