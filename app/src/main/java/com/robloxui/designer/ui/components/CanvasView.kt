package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.model.PropValue
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * The main design canvas that renders a preview of all GUI elements.
 * Features: pinch-to-zoom, pan, grid, element selection.
 */
@Composable
fun CanvasView(
    rootElement: GuiElement,
    selectedElementId: String?,
    zoom: Float,
    panX: Float,
    panY: Float,
    onSelect: (String?) -> Unit,
    onMoveElement: (String, Float, Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .background(StudioColors.BackgroundCanvas)
            .clipToBounds()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .drawBehind {
                drawCanvasGrid(canvasSize, zoom)
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    if (gestureZoom != 1f) {
                        onZoomChange((zoom * gestureZoom).coerceIn(0.1f, 5f))
                    }
                    if (pan.x != 0f || pan.y != 0f) {
                        onPanChange(panX + pan.x, panY + pan.y)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val worldX = (offset.x - panX) / zoom
                    val worldY = (offset.y - panY) / zoom
                    val hit = hitTest(rootElement, worldX, worldY)
                    onSelect(hit?.id ?: rootElement.id)
                }
            }
    ) {
        // Canvas content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = panX.dp, y = panY.dp)
                .scale(zoom)
        ) {
            // Render the GUI tree
            CanvasElementRenderer(
                element = rootElement,
                selectedElementId = selectedElementId,
                canvasWidth = with(LocalDensity.current) { canvasSize.width.toDp() },
                canvasHeight = with(LocalDensity.current) { canvasSize.height.toDp() },
                onSelect = onSelect
            )
        }

        // Top-left info overlays
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CanvasInfoBadge("Zoom: ${(zoom * 100).toInt()}%")
            CanvasInfoBadge("${countElements(rootElement)} instances")
        }

        // Zoom controls (bottom-right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallIconButton(
                icon = Icons.Filled.ZoomOut,
                onClick = { onZoomChange((zoom - 0.1f).coerceAtLeast(0.1f)) }
            )
            SmallIconButton(
                icon = Icons.Filled.ZoomIn,
                onClick = { onZoomChange((zoom + 0.1f).coerceAtMost(5f)) }
            )
            SmallIconButton(
                icon = Icons.Filled.FitScreen,
                onClick = { onZoomChange(1f); onPanChange(0f, 0f) }
            )
        }

        // Pan hint
        if (zoom != 1f || panX != 0f || panY != 0f) {
            Text(
                "Drag to pan \u00B7 Pinch to zoom",
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(
                        StudioColors.Background.copy(alpha = 0.7f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CanvasInfoBadge(text: String) {
    Text(
        text = text,
        style = StudioTypography.MonoSmall,
        color = StudioColors.TextTertiary,
        modifier = Modifier
            .background(StudioColors.Background.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun SmallIconButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(StudioColors.Background.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = StudioColors.TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Recursively renders GUI elements on the canvas preview.
 */
@Composable
private fun CanvasElementRenderer(
    element: GuiElement,
    selectedElementId: String?,
    canvasWidth: Dp,
    canvasHeight: Dp,
    onSelect: (String?) -> Unit
) {
    if (!element.visible) return

    val position = element.prop("Position")?.value as? PropValue.UDim2Value
    val size = element.prop("Size")?.value as? PropValue.UDim2Value

    val posX = if (position != null) calculateUDim(position.xScale, position.xOffset, canvasWidth)
               else 0.dp
    val posY = if (position != null) calculateUDim(position.yScale, position.yOffset, canvasHeight)
               else 0.dp
    val elemWidth = if (size != null) calculateUDim(size.xScale, size.xOffset, canvasWidth)
                    else canvasWidth
    val elemHeight = if (size != null) calculateUDim(size.yScale, size.yOffset, canvasHeight)
                     else 20.dp

    val isSelected = element.id == selectedElementId

    Column(modifier = Modifier.offset(x = posX, y = posY)) {
        Box(
            modifier = Modifier
                .width(if (elemWidth > 0.dp) elemWidth else 0.dp)
                .height(if (elemHeight > 0.dp) elemHeight else 0.dp)
                .then(getBoxStyle(element))
                .then(
                    if (isSelected) Modifier.border(2.dp, StudioColors.CanvasSelection)
                    else Modifier
                )
                .clickable { onSelect(element.id) }
        ) {
            // Element type label
            Text(
                text = "${element.type.displayName}[${element.name}]",
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(StudioColors.Background.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )

            // Text content for text-based elements
            when (element.type) {
                ElementType.TEXT_LABEL, ElementType.BUTTON, ElementType.TEXT_BOX -> {
                    val text = element.prop("Text")?.value as? PropValue.StringValue
                    val textColor = element.prop("TextColor3")?.value as? PropValue.ColorValue
                    if (text != null) {
                        Text(
                            text = text.value,
                            color = textColor?.value ?: Color.White,
                            style = StudioTypography.MonoText,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(4.dp)
                        )
                    }
                }
                ElementType.IMAGE_LABEL, ElementType.IMAGE_BUTTON -> {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        tint = StudioColors.TextTertiary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                    )
                }
                else -> {}
            }

            // Render children
            if (element.children.isNotEmpty() && element.expanded) {
                element.children.forEach { child ->
                    CanvasElementRenderer(
                        element = child,
                        selectedElementId = selectedElementId,
                        canvasWidth = if (elemWidth > 0.dp) elemWidth else canvasWidth,
                        canvasHeight = if (elemHeight > 0.dp) elemHeight else canvasHeight,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

private fun getBoxStyle(element: GuiElement): Modifier {
    val bgColor = element.prop("BackgroundColor3")?.value as? PropValue.ColorValue
    val bgTrans = element.prop("BackgroundTransparency")?.value as? PropValue.FloatValue
    val borderSize = element.prop("BorderSizePixel")?.value as? PropValue.IntValue
    val borderColor = element.prop("BorderColor3")?.value as? PropValue.ColorValue

    var mod = Modifier

    if (bgColor != null) {
        val alpha = 1f - (bgTrans?.value ?: 0f)
        mod = mod.background(bgColor.value.copy(alpha = alpha.coerceIn(0f, 1f)))
    }

    if (borderSize != null && borderSize.value > 0 && borderColor != null) {
        mod = mod.border(borderSize.value.dp, borderColor.value)
    }

    return mod
}

/**
 * Draw the Roblox Studio-style grid on the canvas background.
 */
private fun DrawScope.drawCanvasGrid(canvasSize: Size, zoom: Float) {
    val gridMinorPx = 20.dp.toPx() * zoom
    val gridMajorPx = 100.dp.toPx() * zoom

    if (gridMinorPx < 4f) return // too zoomed out to show minor grid

    val right = canvasSize.width
    val bottom = canvasSize.height

    // Minor grid lines
    var x = 0f
    while (x <= right) {
        drawLine(
            color = StudioColors.CanvasGridMinor,
            start = Offset(x, 0f),
            end = Offset(x, bottom),
            strokeWidth = 0.5f
        )
        x += gridMinorPx
    }
    var y = 0f
    while (y <= bottom) {
        drawLine(
            color = StudioColors.CanvasGridMinor,
            start = Offset(0f, y),
            end = Offset(right, y),
            strokeWidth = 0.5f
        )
        y += gridMinorPx
    }

    // Major grid lines
    x = 0f
    while (x <= right) {
        drawLine(
            color = StudioColors.CanvasGridMajor,
            start = Offset(x, 0f),
            end = Offset(x, bottom),
            strokeWidth = 1.5f
        )
        x += gridMajorPx
    }
    y = 0f
    while (y <= bottom) {
        drawLine(
            color = StudioColors.CanvasGridMajor,
            start = Offset(0f, y),
            end = Offset(right, y),
            strokeWidth = 1.5f
        )
        y += gridMajorPx
    }

    // Center crosshair
    val cx = right / 2
    val cy = bottom / 2
    drawLine(Color(0xFF00B4FF).copy(alpha = 0.3f), Offset(cx - 10f, cy), Offset(cx + 10f, cy), 1f)
    drawLine(Color(0xFF00B4FF).copy(alpha = 0.3f), Offset(cx, cy - 10f), Offset(cx, cy + 10f), 1f)
}

private fun calculateUDim(scale: Float, offset: Float, parentSize: Dp): Dp {
    return (parentSize * scale) + offset.dp
}

/**
 * Simple hit-test: walk the tree in reverse order (topmost first).
 */
private fun hitTest(element: GuiElement, worldX: Float, worldY: Float): GuiElement? {
    if (!element.visible) return null
    for (child in element.children.reversed()) {
        val hit = hitTest(child, worldX, worldY)
        if (hit != null) return hit
    }
    return null
}

private fun countElements(element: GuiElement): Int {
    return 1 + element.children.sumOf { countElements(it) }
}
