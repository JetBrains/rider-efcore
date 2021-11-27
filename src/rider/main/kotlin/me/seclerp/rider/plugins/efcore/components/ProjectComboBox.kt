package me.seclerp.rider.plugins.efcore.components

import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.Cell
import com.jetbrains.rd.ide.model.ProjectInfo
import java.awt.event.ItemEvent
import javax.swing.ComboBoxModel

fun Cell.projectComboBox(model: ComboBoxModel<ProjectInfo>, getter: () -> ProjectInfo?, setter: (ProjectInfo?) -> Unit) =
    comboBox(model,
        getter,
        setter,
        ProjectInfoComboBoxRendererAdapter())
        .constraints(CCFlags.pushX, CCFlags.growX)
        // Setter provided above called only on submit, so we need additional change detection
        .component.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                setter(it.item as ProjectInfo)
            }
        }