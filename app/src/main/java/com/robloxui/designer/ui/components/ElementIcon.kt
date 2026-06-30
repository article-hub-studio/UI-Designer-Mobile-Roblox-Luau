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
import androidx.compose.runtime.remember
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
fun getVanillaIcon(type: ElementType): Triple<Int, ImageVector, Color> {
    return when (type) {
        ElementType.FRAME -> Triple(R.drawable.instance_frame, Icons.Filled.CropSquare, StudioColors.ExplorerIconContainer)
        ElementType.SCOPING_FRAME -> Triple(R.drawable.instance_scrollingframe, Icons.Filled.ViewStream, StudioColors.ExplorerIconContainer)
        ElementType.CANVAS -> Triple(R.drawable.instance_canvasgroup, Icons.Filled.Layers, StudioColors.ExplorerIconContainer)
        ElementType.BUTTON -> Triple(R.drawable.instance_textbutton, Icons.Filled.SmartButton, StudioColors.ExplorerIconButton)
        ElementType.IMAGE_BUTTON -> Triple(R.drawable.instance_imagebutton, Icons.Filled.Image, StudioColors.ExplorerIconButton)
        ElementType.TEXT_LABEL -> Triple(R.drawable.instance_textlabel, Icons.Filled.TextFields, StudioColors.ExplorerIconLabel)
        ElementType.TEXT_BOX -> Triple(R.drawable.instance_textbox, Icons.Filled.EditNote, StudioColors.ExplorerIconLabel)
        ElementType.IMAGE_LABEL -> Triple(R.drawable.instance_imagelabel, Icons.Filled.Image, StudioColors.ExplorerIconImage)
        ElementType.VIEWPORT_FRAME -> Triple(R.drawable.instance_viewportframe, Icons.Filled.ViewInAr, StudioColors.ExplorerIconImage)
        ElementType.VIDEO_FRAME -> Triple(R.drawable.instance_videoframe, Icons.Filled.Videocam, StudioColors.ExplorerIconImage)
        ElementType.UI_LIST_LAYOUT -> Triple(R.drawable.instance_uilistlayout, Icons.Filled.FormatListBulleted, StudioColors.ExplorerIconLayout)
        ElementType.UI_GRID_LAYOUT -> Triple(R.drawable.instance_uigridlayout, Icons.Filled.GridView, StudioColors.ExplorerIconLayout)
        ElementType.UI_TABLE_LAYOUT -> Triple(R.drawable.instance_uitablelayout, Icons.Filled.TableChart, StudioColors.ExplorerIconLayout)
        ElementType.UI_PADDING -> Triple(R.drawable.instance_uipadding, Icons.Filled.SpaceBar, StudioColors.ExplorerIconLayout)
        ElementType.UI_CORNER -> Triple(R.drawable.instance_uicorner, Icons.Filled.RoundedCorner, StudioColors.ExplorerIconDeco)
        ElementType.UI_STROKE -> Triple(R.drawable.instance_uistroke, Icons.Filled.BorderStyle, StudioColors.ExplorerIconDeco)
        ElementType.UI_GRADIENT -> Triple(R.drawable.instance_uigradient, Icons.Filled.Gradient, StudioColors.ExplorerIconDeco)
    }
}

/**
 * Returns the appropriate icon and color for each Roblox element type.
 * Uses Vanilla Roblox icons when available, with Material fallback.
 */
fun getElementIcon(type: ElementType): ElementIconData {
    val (_, fallbackIcon, color) = getVanillaIcon(type)
    // For colored element type dots and quick references, return the Material fallback with color
    return ElementIconData(fallbackIcon, color)
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
    val (resId, fallbackIcon, iconColor) = getVanillaIcon(type)
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasResource = remember(resId) {
        try {
            context.resources.getDrawable(resId, context.theme)
            true
        } catch (e: android.content.res.Resources.NotFoundException) {
            false
        }
    }

    if (hasResource) {
        Icon(
            painter = painterResource(id = resId),
            contentDescription = type.displayName,
            tint = Color.Unspecified,
            modifier = modifier.size(size)
        )
    } else {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = type.displayName,
            tint = iconColor,
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
