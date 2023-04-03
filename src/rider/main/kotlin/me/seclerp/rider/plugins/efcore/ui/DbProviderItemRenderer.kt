package me.seclerp.rider.plugins.efcore.ui

import com.intellij.execution.runToolbar.components.TrimmedMiddleLabel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.UIUtil
import me.seclerp.rider.plugins.efcore.ui.items.DbProviderItem
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

class DbProviderItemRenderer : ListCellRenderer<DbProviderItem> {
    private val packageIdComponent = createColumnComponent().apply { foreground = JBColor.BLACK }
    private val packageVersionComponent = createColumnComponent().apply { foreground = JBColor.GRAY }

    private val rowComponent = createRowComponent().apply {
        add(packageIdComponent, GridBagConstraints(
            0, 0,
            1, 1,
            1.0, 0.0,
            GridBagConstraints.BASELINE,
            GridBagConstraints.HORIZONTAL,
            JBInsets.emptyInsets(),
            0, 0
        ))
        add(packageVersionComponent, GridBagConstraints(
            1, 0,
            1, 1,
            0.0, 0.0,
            GridBagConstraints.BASELINE,
            GridBagConstraints.NONE,
            JBInsets.emptyInsets(),
            0, 0
        ))
    }

    override fun getListCellRendererComponent(list: JList<out DbProviderItem>?, value: DbProviderItem?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        rowComponent.apply {
            background = if (isSelected) list?.selectionBackground else list?.background
            if (isEnabled != list?.isEnabled) {
                UIUtil.setEnabled(this, list?.isEnabled ?: false, true)
            }
        }

        packageIdComponent.icon = value?.icon
        packageIdComponent.text = value?.data?.id
        packageVersionComponent.text = value?.data?.version

        return rowComponent
    }

    private fun createRowComponent() =
        JPanel(GridBagLayout()).apply {
            border = IdeBorderFactory.createEmptyBorder(insets)
        }

    private fun createColumnComponent() =
        TrimmedMiddleLabel().apply {
            isOpaque = false
            border = null
        }
}