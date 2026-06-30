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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.robloxui.designer.R
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.ui.theme.StudioColors

/**
 * Maps Roblox element types to Vanilla-style icons from the icon pack.
 */
data class ElementIconData(
    val icon: ImageVector,
    val color: Color
)

data class VanillaIconData(
    val drawableId: Int,
    val fallbackIcon: ImageVector,
    val color: Color
)

/**
 * Get Vanilla icon for an element type with fallback to Material icon.
 */
fun getVanillaIcon(type: ElementType): VanillaIconData {
    val (drawableId, fallbackIcon, color) = when (type) {
        ElementType.FRAME -> R.drawable.instance_frame to (Icons.Filled.CropSquare as ImageVector) to StudioColors.ExplorerIconContainer
        ElementType.SCOPING_FRAME -> R.drawable.instance_scrollingframe to (Icons.Filled.ViewStream as ImageVector) to StudioColors.ExplorerIconContainer
        ElementType.CANVAS -> R.drawable.instance_canvasgroup to (Icons.Filled.Layers as ImageVector) to StudioColors.ExplorerIconContainer
        ElementType.BUTTON -> R.drawable.instance_textbutton to (Icons.Filled.SmartButton as ImageVector) to StudioColors.ExplorerIconButton
        ElementType.IMAGE_BUTTON -> R.drawable.instance_imagebutton to (Icons.Filled.Image as ImageVector) to StudioColors.ExplorerIconButton
        ElementType.TEXT_LABEL -> R.drawable.instance_textlabel to (Icons.Filled.TextFields as ImageVector) to StudioColors.ExplorerIconLabel
        ElementType.TEXT_BOX -> R.drawable.instance_textbox to (Icons.Filled.EditNote as ImageVector) to StudioColors.ExplorerIconLabel
        ElementType.IMAGE_LABEL -> R.drawable.instance_imagelabel to (Icons.Filled.Image as ImageVector) to StudioColors.ExplorerIconImage
        ElementType.VIEWPORT_FRAME -> R.drawable.instance_viewportframe to (Icons.Filled.ViewInAr as ImageVector) to StudioColors.ExplorerIconImage
        ElementType.VIDEO_FRAME -> R.drawable.instance_videoframe to (Icons.Filled.Videocam as ImageVector) to StudioColors.ExplorerIconImage
        ElementType.UI_LIST_LAYOUT -> R.drawable.instance_uilistlayout to (Icons.Filled.FormatListBulleted as ImageVector) to StudioColors.ExplorerIconLayout
        ElementType.UI_GRID_LAYOUT -> R.drawable.instance_uigridlayout to (Icons.Filled.GridView as ImageVector) to StudioColors.ExplorerIconLayout
        ElementType.UI_TABLE_LAYOUT -> R.drawable.instance_uitablelayout to (Icons.Filled.TableChart as ImageVector) to StudioColors.ExplorerIconLayout
        ElementType.UI_PADDING -> R.drawable.instance_uipadding to (Icons.Filled.SpaceBar as ImageVector) to StudioColors.ExplorerIconLayout
        ElementType.UI_CORNER -> R.drawable.instance_uicorner to (Icons.Filled.RoundedCorner as ImageVector) to StudioColors.ExplorerIconDeco
        ElementType.UI_STROKE -> R.drawable.instance_uistroke to (Icons.Filled.BorderStyle as ImageVector) to StudioColors.ExplorerIconDeco
        ElementType.UI_GRADIENT -> R.drawable.instance_uigradient to (Icons.Filled.Gradient as ImageVector) to StudioColors.ExplorerIconDeco
    }
    return VanillaIconData(drawableId, fallbackIcon, color)
}

/**
 * Returns the appropriate icon and color for each Roblox element type.
 * Uses Vanilla Roblox icons when available, with Material fallback.
 */
fun getElementIcon(type: ElementType): ElementIconData {
    val vanilla = getVanillaIcon(type)
    // For colored element type dots and quick references, return the Material fallback with color
    return ElementIconData(vanilla.fallbackIcon, vanilla.color)
}

/**
 * Composable that renders the Vanilla icon for an element type.
 * Falls back to a colored Material icon if the Vanilla resource isn't found.
 */
@Composable
fun VanillaElementIcon(
    type: ElementType,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    val vanilla = getVanillaIcon(type)
    val painter: Painter? = try {
        painterResource(id = vanilla.drawableId)
    } catch (e: Exception) {
        null
    }

    if (painter != null) {
        Icon(
            painter = painter,
            contentDescription = type.displayName,
            tint = Color.Unspecified, // Use PNG's original colors
            modifier = modifier.size(size)
        )
    } else {
        // Fallback to Material icon
        Icon(
            imageVector = vanilla.fallbackIcon,
            contentDescription = type.displayName,
            tint = vanilla.color,
            modifier = modifier.size(size)
        )
    }
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
