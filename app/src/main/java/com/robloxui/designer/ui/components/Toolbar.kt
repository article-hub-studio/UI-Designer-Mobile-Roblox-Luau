package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography
import com.robloxui.designer.viewmodel.EditorTool

/**
 * Main toolbar with element palette and actions.
 */
@Composable
fun EditorToolbar(
    currentTool: EditorTool,
    canUndo: Boolean,
    canRedo: Boolean,
    onToolChange: (EditorTool) -> Unit,
    onAddElement: (ElementType) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onExport: () -> Unit,
    onPreview: () -> Unit,
    isPreviewing: Boolean,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioColors.ToolbarBg)
            .width(52.dp)
    ) {
        // Tools section
        ToolSection(title = "Tools") {
            ToolButton(
                icon = Icons.Filled.NearMe,
                label = "Select",
                isActive = currentTool == EditorTool.POINTER,
                onClick = { onToolChange(EditorTool.POINTER) }
            )
            ToolButton(
                icon = Icons.Filled.OpenWith,
                label = "Move",
                isActive = currentTool == EditorTool.MOVE,
                onClick = { onToolChange(EditorTool.MOVE) }
            )
        }

        Divider(
            color = StudioColors.ToolbarDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Element palette
        ToolSection(title = "Add") {
            AddElementButton(
                icon = Icons.Filled.CropSquare,
                label = "Frame",
                type = ElementType.FRAME,
                onClick = { onAddElement(ElementType.FRAME) }
            )
            AddElementButton(
                icon = Icons.Filled.SmartButton,
                label = "Button",
                type = ElementType.BUTTON,
                onClick = { onAddElement(ElementType.BUTTON) }
            )
            AddElementButton(
                icon = Icons.Filled.TextFields,
                label = "Label",
                type = ElementType.TEXT_LABEL,
                onClick = { onAddElement(ElementType.TEXT_LABEL) }
            )
            AddElementButton(
                icon = Icons.Filled.Image,
                label = "Image",
                type = ElementType.IMAGE_LABEL,
                onClick = { onAddElement(ElementType.IMAGE_LABEL) }
            )
            AddElementButton(
                icon = Icons.Filled.EditNote,
                label = "Text Box",
                type = ElementType.TEXT_BOX,
                onClick = { onAddElement(ElementType.TEXT_BOX) }
            )
            AddElementButton(
                icon = Icons.Filled.ViewStream,
                label = "Scrolling",
                type = ElementType.SCOPING_FRAME,
                onClick = { onAddElement(ElementType.SCOPING_FRAME) }
            )
        }

        Divider(
            color = StudioColors.ToolbarDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Layout helpers
        ToolSection(title = "Layout") {
            AddElementButton(
                icon = Icons.Filled.FormatListBulleted,
                label = "List",
                type = ElementType.UI_LIST_LAYOUT,
                onClick = { onAddElement(ElementType.UI_LIST_LAYOUT) }
            )
            AddElementButton(
                icon = Icons.Filled.GridView,
                label = "Grid",
                type = ElementType.UI_GRID_LAYOUT,
                onClick = { onAddElement(ElementType.UI_GRID_LAYOUT) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions at bottom
        Divider(
            color = StudioColors.ToolbarDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        ToolSection(title = "") {
            ToolButton(
                icon = Icons.Filled.Undo,
                label = "Undo",
                isActive = false,
                enabled = canUndo,
                onClick = onUndo
            )
            ToolButton(
                icon = Icons.Filled.Redo,
                label = "Redo",
                isActive = false,
                enabled = canRedo,
                onClick = onRedo
            )
        }

        Divider(
            color = StudioColors.ToolbarDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        ToolSection(title = "") {
            ToolButton(
                icon = if (isPreviewing) Icons.Filled.Edit else Icons.Filled.PlayArrow,
                label = if (isPreviewing) "Edit" else "Preview",
                isActive = isPreviewing,
                onClick = onPreview
            )
            ToolButton(
                icon = Icons.Filled.FileDownload,
                label = "Export",
                isActive = false,
                onClick = onExport
            )
            ToolButton(
                icon = Icons.Filled.AddBox,
                label = "New",
                isActive = false,
                onClick = onNewProject
            )
        }
    }
}

@Composable
private fun ToolSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        content()
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor = when {
        isActive -> StudioColors.Primary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val iconColor = when {
        !enabled -> StudioColors.TextDisabled
        isActive -> StudioColors.Primary
        else -> StudioColors.ToolbarIcon
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AddElementButton(
    icon: ImageVector,
    label: String,
    type: ElementType,
    onClick: () -> Unit
) {
    val elementIconData = getElementIcon(type)

    Box(
        modifier = Modifier
            .size(36.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = elementIconData.color,
            modifier = Modifier.size(20.dp)
        )
    }
}
