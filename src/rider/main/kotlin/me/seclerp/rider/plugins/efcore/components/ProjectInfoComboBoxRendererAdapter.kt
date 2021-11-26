package me.seclerp.rider.plugins.efcore.components

import com.jetbrains.rd.ide.model.ProjectInfo
import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import java.awt.Component
import java.io.File
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.ListCellRenderer

class ProjectInfoComboBoxRendererAdapter: ListCellRenderer<ProjectInfo?> {
    private val trueRenderer = DefaultListCellRenderer()

    override fun getListCellRendererComponent(
        list: JList<out ProjectInfo>?,
        value: ProjectInfo?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        trueRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        trueRenderer.text = value!!.name
        trueRenderer.icon = DotnetIconResolver.resolveForExtension(File(value.fullPath).extension)

        return trueRenderer
    }
}