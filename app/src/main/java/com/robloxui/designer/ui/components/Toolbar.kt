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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography
import com.robloxui.designer.viewmodel.EditorTool

/**
 * Compact Figma-style toolbar.
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
            .width(40.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Select & Move tools
        CompactToolButton(
            icon = Icons.Filled.NearMe,
            isActive = currentTool == EditorTool.POINTER,
            onClick = { onToolChange(EditorTool.POINTER) }
        )
        CompactToolButton(
            icon = Icons.Filled.OpenWith,
            isActive = currentTool == EditorTool.MOVE,
            onClick = { onToolChange(EditorTool.MOVE) }
        )

        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp, modifier = Modifier.padding(horizontal = 6.dp))
        Spacer(modifier = Modifier.height(4.dp))

        // Quick add elements
        CompactAddButton(
            icon = Icons.Filled.CropSquare,
            type = ElementType.FRAME,
            onClick = { onAddElement(ElementType.FRAME) }
        )
        CompactAddButton(
            icon = Icons.Filled.SmartButton,
            type = ElementType.BUTTON,
            onClick = { onAddElement(ElementType.BUTTON) }
        )
        CompactAddButton(
            icon = Icons.Filled.TextFields,
            type = ElementType.TEXT_LABEL,
            onClick = { onAddElement(ElementType.TEXT_LABEL) }
        )
        CompactAddButton(
            icon = Icons.Filled.Image,
            type = ElementType.IMAGE_LABEL,
            onClick = { onAddElement(ElementType.IMAGE_LABEL) }
        )
        CompactAddButton(
            icon = Icons.Filled.EditNote,
            type = ElementType.TEXT_BOX,
            onClick = { onAddElement(ElementType.TEXT_BOX) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom actions
        CompactToolButton(
            icon = Icons.Filled.Undo,
            isActive = false,
            enabled = canUndo,
            onClick = onUndo
        )
        CompactToolButton(
            icon = Icons.Filled.Redo,
            isActive = false,
            enabled = canRedo,
            onClick = onRedo
        )

        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp, modifier = Modifier.padding(horizontal = 6.dp))
        Spacer(modifier = Modifier.height(4.dp))

        CompactToolButton(
            icon = if (isPreviewing) Icons.Filled.Edit else Icons.Filled.PlayArrow,
            isActive = isPreviewing,
            onClick = onPreview
        )
        CompactToolButton(
            icon = Icons.Filled.FileDownload,
            isActive = false,
            onClick = onExport
        )
        CompactToolButton(
            icon = Icons.Filled.AddBox,
            isActive = false,
            onClick = onNewProject
        )
    }
}

@Composable
private fun CompactToolButton(
    icon: ImageVector,
    isActive: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) StudioColors.Primary.copy(alpha = 0.2f) else Color.Transparent
    val iconColor = when {
        !enabled -> StudioColors.TextDisabled
        isActive -> StudioColors.Primary
        else -> StudioColors.ToolbarIcon
    }
    Box(
        modifier = Modifier
            .size(30.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CompactAddButton(
    icon: ImageVector,
    type: ElementType,
    onClick: () -> Unit
) {
    val elementIconData = getElementIcon(type)
    Box(
        modifier = Modifier
            .size(30.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = elementIconData.color, modifier = Modifier.size(16.dp))
    }
}
