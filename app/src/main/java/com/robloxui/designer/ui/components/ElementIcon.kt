package com.robloxui.designer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.ui.theme.StudioColors

/**
 * Returns the appropriate icon and color for each Roblox element type.
 */
data class ElementIconData(
    val icon: ImageVector,
    val color: Color
)

fun getElementIcon(type: ElementType): ElementIconData = when (type) {
    ElementType.FRAME -> ElementIconData(Icons.Filled.CropSquare, StudioColors.ExplorerIconContainer)
    ElementType.SCOPING_FRAME -> ElementIconData(Icons.Filled.ViewStream, StudioColors.ExplorerIconContainer)
    ElementType.CANVAS -> ElementIconData(Icons.Filled.Layers, StudioColors.ExplorerIconContainer)
    ElementType.BUTTON -> ElementIconData(Icons.Filled.SmartButton, StudioColors.ExplorerIconButton)
    ElementType.IMAGE_BUTTON -> ElementIconData(Icons.Filled.Image, StudioColors.ExplorerIconButton)
    ElementType.TEXT_LABEL -> ElementIconData(Icons.Filled.TextFields, StudioColors.ExplorerIconLabel)
    ElementType.TEXT_BOX -> ElementIconData(Icons.Filled.EditNote, StudioColors.ExplorerIconLabel)
    ElementType.IMAGE_LABEL -> ElementIconData(Icons.Filled.Image, StudioColors.ExplorerIconImage)
    ElementType.VIEWPORT_FRAME -> ElementIconData(Icons.Filled.ViewInAr, StudioColors.ExplorerIconImage)
    ElementType.VIDEO_FRAME -> ElementIconData(Icons.Filled.Videocam, StudioColors.ExplorerIconImage)
    ElementType.UI_LIST_LAYOUT -> ElementIconData(Icons.Filled.FormatListBulleted, StudioColors.ExplorerIconLayout)
    ElementType.UI_GRID_LAYOUT -> ElementIconData(Icons.Filled.GridView, StudioColors.ExplorerIconLayout)
    ElementType.UI_TABLE_LAYOUT -> ElementIconData(Icons.Filled.TableChart, StudioColors.ExplorerIconLayout)
    ElementType.UI_PADDING -> ElementIconData(Icons.Filled.SpaceBar, StudioColors.ExplorerIconLayout)
    ElementType.UI_CORNER -> ElementIconData(Icons.Filled.RoundedCorner, StudioColors.ExplorerIconDeco)
    ElementType.UI_STROKE -> ElementIconData(Icons.Filled.BorderStyle, StudioColors.ExplorerIconDeco)
    ElementType.UI_GRADIENT -> ElementIconData(Icons.Filled.Gradient, StudioColors.ExplorerIconDeco)
}

@Composable
fun ElementTypeIcon(
    type: ElementType,
    modifier: Modifier = Modifier,
    size: Int = 18
) {
    val data = getElementIcon(type)
    Icon(
        imageVector = data.icon,
        contentDescription = type.displayName,
        tint = data.color,
        modifier = modifier.size(size.dp)
    )
}

/**
 * Small colored dot indicator used in the explorer tree.
 */
@Composable
fun ElementTypeDot(
    type: ElementType,
    modifier: Modifier = Modifier,
    size: Int = 8
) {
    val data = getElementIcon(type)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(data.color)
    )
}
