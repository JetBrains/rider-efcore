package me.seclerp.rider.plugins.efcore.components

import me.seclerp.rider.plugins.efcore.DotnetIconResolver
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.ListCellRenderer

class ProjectInfoComboBoxRendererAdapter: ListCellRenderer<String?> {
    private val trueRenderer = DefaultListCellRenderer()

    override fun getListCellRendererComponent(
        list: JList<out String>?,
        value: String?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        trueRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        trueRenderer.text = value!!
        // TODO Replace with actual project file name
        trueRenderer.icon = DotnetIconResolver.resolveForExtension("csproj")

        return trueRenderer
    }
}