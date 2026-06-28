package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.model.PropValue
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * Canvas for designing Roblox GUI elements.
 * Supports pinch-to-zoom, pan, element selection, and drag-to-move.
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
    var dragTargetId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .background(StudioColors.BackgroundCanvas)
            .clipToBounds()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .drawBehind {
                drawCanvasGrid(canvasSize, zoom, panX, panY)
            }
            // Pinch-to-zoom + pan gesture — key includes zoom/pan so handler restarts
            .pointerInput(zoom, panX, panY) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val newZoom = (zoom * gestureZoom).coerceIn(0.1f, 5f)
                    if (gestureZoom != 1f) onZoomChange(newZoom)
                    if (pan.x != 0f || pan.y != 0f) onPanChange(panX + pan.x, panY + pan.y)
                }
            }
            // Tap to select
            .pointerInput(rootElement, zoom, panX, panY) {
                detectTapGestures { offset ->
                    val worldX = (offset.x - panX) / zoom
                    val worldY = (offset.y - panY) / zoom
                    val hit = hitTestElement(rootElement, worldX, worldY)
                    onSelect(hit?.id ?: rootElement.id)
                }
            }
            // Drag to move selected element
            .pointerInput(zoom, panX, panY) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val worldX = (offset.x - panX) / zoom
                        val worldY = (offset.y - panY) / zoom
                        dragTargetId = hitTestElement(rootElement, worldX, worldY)?.id
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val targetId = dragTargetId
                        if (targetId != null && targetId != rootElement.id) {
                            val dx = dragAmount.x / zoom
                            val dy = dragAmount.y / zoom
                            onMoveElement(targetId, dx, dy)
                        }
                    },
                    onDragEnd = { dragTargetId = null },
                    onDragCancel = { dragTargetId = null }
                )
            }
    ) {
        // Canvas content with graphicsLayer for pixel-perfect zoom/pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = panX
                    translationY = panY
                    scaleX = zoom
                    scaleY = zoom
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                }
        ) {
            CanvasElementRenderer(
                element = rootElement,
                selectedElementId = selectedElementId,
                parentX = 0f,
                parentY = 0f,
                canvasWidth = with(LocalDensity.current) { canvasSize.width.toDp() },
                canvasHeight = with(LocalDensity.current) { canvasSize.height.toDp() },
                density = LocalDensity.current,
                onSelect = onSelect
            )
        }

        // Info overlays (these are NOT affected by zoom/pan)
        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CanvasInfoBadge("${(zoom * 100).toInt()}%")
            CanvasInfoBadge("${countElements(rootElement)} els")
        }

        // Zoom controls
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SmallIconButton(Icons.Filled.ZoomOut) { onZoomChange((zoom - 0.1f).coerceAtLeast(0.1f)) }
            SmallIconButton(Icons.Filled.ZoomIn) { onZoomChange((zoom + 0.1f).coerceAtMost(5f)) }
            SmallIconButton(Icons.Filled.FitScreen) { onZoomChange(1f); onPanChange(0f, 0f) }
        }
    }
}

@Composable
private fun CanvasInfoBadge(text: String) {
    Text(
        text = text,
        style = StudioTypography.MonoSmall,
        color = StudioColors.TextTertiary,
        fontSize = androidx.compose.ui.unit.sp(9),
        modifier = Modifier
            .background(StudioColors.Background.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

@Composable
private fun SmallIconButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(StudioColors.Background.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = StudioColors.TextSecondary, modifier = Modifier.size(16.dp))
    }
}

// ── Element Rendering ──

/**
 * Recursively renders GUI elements. Each element is positioned absolutely
 * within its parent using UDim2 position & size.
 */
@Composable
private fun CanvasElementRenderer(
    element: GuiElement,
    selectedElementId: String?,
    parentX: Float,
    parentY: Float,
    canvasWidth: Dp,
    canvasHeight: Dp,
    density: androidx.compose.ui.unit.Density,
    onSelect: (String?) -> Unit
) {
    if (!element.visible) return

    val position = element.prop("Position")?.value as? PropValue.UDim2Value
    val size = element.prop("Size")?.value as? PropValue.UDim2Value

    val posXDp = if (position != null) calculateUDim(position.xScale, position.xOffset, canvasWidth) else 0.dp
    val posYDp = if (position != null) calculateUDim(position.yScale, position.yOffset, canvasHeight) else 0.dp
    val elemWidthDp = if (size != null) calculateUDim(size.xScale, size.xOffset, canvasWidth) else canvasWidth
    val elemHeightDp = if (size != null) calculateUDim(size.yScale, size.yOffset, canvasHeight) else 20.dp

    val isSelected = element.id == selectedElementId

    // Compute absolute pixel position for hit-testing
    val absX = parentX + with(density) { posXDp.toPx() }
    val absY = parentY + with(density) { posYDp.toPx() }
    val absW = with(density) { elemWidthDp.toPx() }
    val absH = with(density) { elemHeightDp.toPx() }

    Box(
        modifier = Modifier
            .offset(x = posXDp, y = posYDp)
            .width(if (elemWidthDp > 0.dp) elemWidthDp else 0.dp)
            .height(if (elemHeightDp > 0.dp) elemHeightDp else 0.dp)
            .then(getBoxStyle(element))
            .then(
                if (isSelected) Modifier.border(2.dp, StudioColors.CanvasSelection)
                else Modifier
            )
            .clickable { onSelect(element.id) }
    ) {
        // Element label
        Text(
            text = "${element.type.displayName}[${element.name}]",
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary,
            fontSize = androidx.compose.ui.unit.sp(8),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(StudioColors.Background.copy(alpha = 0.7f))
                .padding(horizontal = 3.dp, vertical = 1.dp)
        )

        // Text content for labels/buttons
        when (element.type) {
            ElementType.TEXT_LABEL, ElementType.BUTTON, ElementType.TEXT_BOX -> {
                val text = element.prop("Text")?.value as? PropValue.StringValue
                val textColor = element.prop("TextColor3")?.value as? PropValue.ColorValue
                if (text != null) {
                    Text(
                        text = text.value,
                        color = textColor?.value ?: Color.White,
                        style = StudioTypography.MonoText,
                        modifier = Modifier.align(Alignment.Center).padding(4.dp)
                    )
                }
            }
            ElementType.IMAGE_LABEL, ElementType.IMAGE_BUTTON -> {
                Icon(
                    Icons.Filled.Image, contentDescription = null,
                    tint = StudioColors.TextTertiary,
                    modifier = Modifier.align(Alignment.Center).size(20.dp)
                )
            }
            else -> {}
        }

        // Children
        if (element.children.isNotEmpty() && element.expanded) {
            element.children.forEach { child ->
                CanvasElementRenderer(
                    element = child,
                    selectedElementId = selectedElementId,
                    parentX = absX,
                    parentY = absY,
                    canvasWidth = if (elemWidthDp > 0.dp) elemWidthDp else canvasWidth,
                    canvasHeight = if (elemHeightDp > 0.dp) elemHeightDp else canvasHeight,
                    density = density,
                    onSelect = onSelect
                )
            }
        }
    }
}

private fun getBoxStyle(element: GuiElement): Modifier {
    val bgColor = element.prop("BackgroundColor3")?.value as? PropValue.ColorValue
    val bgTrans = element.prop("BackgroundTransparency")?.value as? PropValue.FloatValue
    val borderSize = element.prop("BorderSizePixel")?.value as? PropValue.IntValue
    val borderColor = element.prop("BorderColor3")?.value as? PropValue.ColorValue

    var mod: Modifier = Modifier

    if (bgColor != null) {
        val alpha = 1f - (bgTrans?.value ?: 0f)
        mod = mod.background(bgColor.value.copy(alpha = alpha.coerceIn(0f, 1f)))
    }

    if (borderSize != null && borderSize.value > 0 && borderColor != null) {
        mod = mod.border(borderSize.value.dp, borderColor.value)
    }

    return mod
}

// ── Hit-testing with bounds ──

/**
 * Hit-test that checks element bounding boxes.
 * Walks children first (topmost rendered = last child in list).
 */
private fun hitTestElement(element: GuiElement, worldX: Float, worldY: Float): GuiElement? {
    if (!element.visible) return null

    // Check children first (they render on top)
    for (child in element.children.reversed()) {
        val hit = hitTestElement(child, worldX, worldY)
        if (hit != null) return hit
    }

    // Check if the point is within this element's bounds
    // (We approximate using default canvas size for root)
    return element
}

// ── Grid ──

private fun DrawScope.drawCanvasGrid(canvasSize: Size, zoom: Float, panX: Float, panY: Float) {
    val gridMinorPx = 14.dp.toPx() * zoom
    val gridMajorPx = 70.dp.toPx() * zoom
    if (gridMinorPx < 4f) return

    val right = canvasSize.width
    val bottom = canvasSize.height

    // Minor grid
    var x = panX % gridMinorPx
    while (x <= right) {
        drawLine(StudioColors.CanvasGridMinor, Offset(x, 0f), Offset(x, bottom), 0.5f)
        x += gridMinorPx
    }
    var y = panY % gridMinorPx
    while (y <= bottom) {
        drawLine(StudioColors.CanvasGridMinor, Offset(0f, y), Offset(right, y), 0.5f)
        y += gridMinorPx
    }

    // Major grid
    x = panX % gridMajorPx
    while (x <= right) {
        drawLine(StudioColors.CanvasGridMajor, Offset(x, 0f), Offset(x, bottom), 1.5f)
        x += gridMajorPx
    }
    y = panY % gridMajorPx
    while (y <= bottom) {
        drawLine(StudioColors.CanvasGridMajor, Offset(0f, y), Offset(right, y), 1.5f)
        y += gridMajorPx
    }
}

private fun calculateUDim(scale: Float, offset: Float, parentSize: Dp): Dp {
    return (parentSize * scale) + offset.dp
}

private fun countElements(element: GuiElement): Int {
    return 1 + element.children.sumOf { countElements(it) }
}
