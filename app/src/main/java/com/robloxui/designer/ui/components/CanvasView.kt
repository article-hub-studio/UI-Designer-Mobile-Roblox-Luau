package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.model.PropValue
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * The main design canvas that renders a preview of all GUI elements.
 * Features: pinch-to-zoom, pan, grid, element selection, drag-to-move.
 *
 * Gesture handling:
 *  - Single-finger drag on elements: moves the element
 *  - Single-finger drag on empty canvas: pans the viewport
 *  - Pinch: zooms in/out
 *  - Tap: selects element
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
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .background(StudioColors.BackgroundCanvas)
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .drawBehind {
                drawCanvasGrid(canvasSize, panX, panY, zoom)
            }
            // Canvas pan: only activates on empty areas or with 2 fingers
            .pointerInput(zoom) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    if (gestureZoom != 1f) {
                        onZoomChange((zoom * gestureZoom).coerceIn(0.1f, 5f))
                    }
                    if (pan.x != 0f || pan.y != 0f) {
                        onPanChange(panX + pan.x, panY + pan.y)
                    }
                }
            }
            // Tap to select
            .pointerInput(rootElement.id) {
                detectTapGestures { offset ->
                    val worldX = (offset.x - panX) / zoom
                    val worldY = (offset.y - panY) / zoom
                    val hit = hitTestElement(rootElement, worldX, worldY, canvasSize, density)
                    onSelect(hit?.id)
                }
            }
    ) {
        // Canvas content area - zoom/pan via graphicsLayer (pixel-space)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = panX
                    translationY = panY
                    scaleX = zoom
                    scaleY = zoom
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            if (rootElement.visible) {
                CanvasElementRenderer(
                    element = rootElement,
                    selectedElementId = selectedElementId,
                    canvasWidth = with(density) { canvasSize.width.toDp() },
                    canvasHeight = with(density) { canvasSize.height.toDp() },
                    onSelect = onSelect,
                    onMoveElement = onMoveElement
                )
            }
        }

        // Top-left info badges
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
                onClick = {
                    onZoomChange(1f)
                    onPanChange(0f, 0f)
                }
            )
        }

        // Pan hint
        if (zoom != 1f || panX != 0f || panY != 0f) {
            Text(
                "Drag to pan · Pinch to zoom",
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Element renderer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CanvasElementRenderer(
    element: GuiElement,
    selectedElementId: String?,
    canvasWidth: Dp,
    canvasHeight: Dp,
    onSelect: (String?) -> Unit = {},
    onMoveElement: (String, Float, Float) -> Unit = { _, _, _ -> }
) {
    if (!element.visible) return

    val isSelected = element.id == selectedElementId

    val elemPos = element.prop("Position")?.value as? PropValue.UDim2Value
    val elemSize = element.prop("Size")?.value as? PropValue.UDim2Value

    val pos = elemPos ?: PropValue.UDim2Value(0f, 0f, 0f, 0f)
    val size = elemSize ?: PropValue.UDim2Value(1f, 0f, 1f, 0f)

    val elemX = calculateUDim(pos.xScale, pos.xOffset, canvasWidth)
    val elemY = calculateUDim(pos.yScale, pos.yOffset, canvasHeight)
    val elemWidth = calculateUDim(size.xScale, size.xOffset, canvasWidth)
    val elemHeight = calculateUDim(size.yScale, size.yOffset, canvasHeight)

    Box(
        modifier = Modifier
            .offset(x = elemX, y = elemY)
            .size(elemWidth, elemHeight)
            .then(getBoxStyle(element))
            .clickable { onSelect(element.id) }
            // Drag to move (only for unlocked, non-root elements)
            .then(
                if (!element.locked) {
                    Modifier.pointerInput(element.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Drag amount is in pixels; convert to dp for UDim2 offset
                                val scaleFactor = this.density
                                onMoveElement(element.id, dragAmount.x / scaleFactor, dragAmount.y / scaleFactor)
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, StudioColors.CanvasSelection)
            )
        }

        // Empty container hint
        if (element.children.isEmpty() && element.type.canHaveChildren) {
            VanillaElementIcon(
                type = element.type,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }

        // Text content for text-based elements
        when (element.type) {
            ElementType.TEXT_LABEL, ElementType.BUTTON, ElementType.TEXT_BOX -> {
                val text = element.prop("Text")?.value as? PropValue.StringValue
                val textColor = element.prop("TextColor3")?.value as? PropValue.ColorValue
                if (text != null && text.value.isNotEmpty()) {
                    Text(
                        text = text.value,
                        color = textColor?.value ?: Color.White,
                        style = StudioTypography.MonoText,
                        fontSize = 11.sp,
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
                    onSelect = onSelect,
                    onMoveElement = onMoveElement
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Style helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun getBoxStyle(element: GuiElement): Modifier {
    val bgColor = element.prop("BackgroundColor3")?.value as? PropValue.ColorValue
    val bgTrans = element.prop("BackgroundTransparency")?.value as? PropValue.FloatValue
    val borderSize = element.prop("BorderSizePixel")?.value as? PropValue.IntValue
    val borderColor = element.prop("BorderColor3")?.value as? PropValue.ColorValue

    var mod: Modifier = Modifier

    // Default semi-transparent background so elements are always visible on canvas
    val bg = bgColor?.value ?: Color(0xFF2D2D44)
    val alpha = 1f - (bgTrans?.value ?: 0.2f)
    mod = mod.background(bg.copy(alpha = alpha.coerceIn(0f, 1f)))

    // Clip to bounds if ClipsDescendants is true
    val clipsDescendants = element.prop("ClipsDescendants")?.value as? PropValue.BoolValue
    if (clipsDescendants?.value == true) {
        mod = mod.graphicsLayer { clip = true }
    }

    if (borderSize != null && borderSize.value > 0 && borderColor != null) {
        mod = mod.border(borderSize.value.dp, borderColor.value)
    }

    return mod
}

// ─────────────────────────────────────────────────────────────────────────────
// Grid drawing
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawCanvasGrid(canvasSize: Size, panX: Float, panY: Float, zoom: Float) {
    val gridMinor = 20.dp.toPx() * zoom
    val gridMajor = 100.dp.toPx() * zoom

    if (gridMinor < 6f) return

    val right = canvasSize.width
    val bottom = canvasSize.height

    val gridOffsetX = panX % gridMinor
    val gridOffsetY = panY % gridMinor
    val gridMajorOffsetX = panX % gridMajor
    val gridMajorOffsetY = panY % gridMajor

    val strokeW = (0.5f * zoom.coerceAtLeast(1f)).coerceAtMost(2f)

    // Minor grid lines
    var x = gridOffsetX
    while (x <= right) {
        drawLine(StudioColors.CanvasGridMinor, Offset(x, 0f), Offset(x, bottom), strokeW)
        x += gridMinor
    }
    var y = gridOffsetY
    while (y <= bottom) {
        drawLine(StudioColors.CanvasGridMinor, Offset(0f, y), Offset(right, y), strokeW)
        y += gridMinor
    }

    // Major grid lines
    val majorStrokeW = (1.5f * zoom.coerceAtLeast(1f)).coerceAtMost(3f)
    x = gridMajorOffsetX
    while (x <= right) {
        drawLine(StudioColors.CanvasGridMajor, Offset(x, 0f), Offset(x, bottom), majorStrokeW)
        x += gridMajor
    }
    y = gridMajorOffsetY
    while (y <= bottom) {
        drawLine(StudioColors.CanvasGridMajor, Offset(0f, y), Offset(right, y), majorStrokeW)
        y += gridMajor
    }

    // Origin crosshair
    val ox = panX
    val oy = panY
    if (ox in -50f..right + 50f && oy in -50f..bottom + 50f) {
        val crossColor = Color(0xFF00B4FF).copy(alpha = 0.3f)
        drawLine(crossColor, Offset(ox - 15f, oy), Offset(ox + 15f, oy), 1.5f)
        drawLine(crossColor, Offset(ox, oy - 15f), Offset(ox, oy + 15f), 1.5f)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UDim2 calculation
// ─────────────────────────────────────────────────────────────────────────────

private fun calculateUDim(scale: Float, offset: Float, parentSize: Dp): Dp {
    return (parentSize * scale) + offset.dp
}

// ─────────────────────────────────────────────────────────────────────────────
// Hit-test with bounds checking
// ─────────────────────────────────────────────────────────────────────────────

private fun hitTestElement(
    element: GuiElement,
    worldX: Float,
    worldY: Float,
    canvasSize: Size,
    density: androidx.compose.ui.unit.Density
): GuiElement? {
    if (!element.visible) return null

    val elemPos = element.prop("Position")?.value as? PropValue.UDim2Value
    val elemSize = element.prop("Size")?.value as? PropValue.UDim2Value

    val pos = elemPos ?: PropValue.UDim2Value(0f, 0f, 0f, 0f)
    val sz = elemSize ?: PropValue.UDim2Value(1f, 0f, 1f, 0f)

    val parentW = canvasSize.width
    val parentH = canvasSize.height

    val px = parentW * pos.xScale + with(density) { pos.xOffset.dp.toPx() }
    val py = parentH * pos.yScale + with(density) { pos.yOffset.dp.toPx() }
    val pw = (parentW * sz.xScale + with(density) { sz.xOffset.dp.toPx() }).coerceAtLeast(0f)
    val ph = (parentH * sz.yScale + with(density) { sz.yOffset.dp.toPx() }).coerceAtLeast(0f)

    for (child in element.children.reversed()) {
        val childCanvasSize = Size(pw, ph)
        val hit = hitTestElement(child, worldX - px, worldY - py, childCanvasSize, density)
        if (hit != null) return hit
    }

    if (worldX in px..(px + pw) && worldY in py..(py + ph)) {
        return element
    }

    return null
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────

private fun countElements(element: GuiElement): Int {
    return 1 + element.children.sumOf { countElements(it) }
}

@Composable
private fun CanvasInfoBadge(text: String) {
    Surface(
        color = StudioColors.BackgroundDarker.copy(alpha = 0.85f),
        shape = RoundedCornerShape(3.dp)
    ) {
        Text(
            text = text,
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SmallIconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(icon, contentDescription = null, tint = StudioColors.TextTertiary, modifier = Modifier.size(16.dp))
    }
}
