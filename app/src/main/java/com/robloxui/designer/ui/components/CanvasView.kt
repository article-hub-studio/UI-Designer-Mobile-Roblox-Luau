package com.robloxui.designer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.model.PropValue
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * Main design canvas with zoom, pan, element selection, drag-to-move, and resize handles.
 *
 * Gesture architecture:
 *  - Single-finger drag on element: moves the element
 *  - Two-finger pinch / drag on empty area: pans/zooms the canvas
 *  - Tap: selects element
 */
@Composable
fun CanvasView(
    rootElement: GuiElement,
    selectedElementId: String?,
    selectedElement: GuiElement?,
    zoom: Float,
    panX: Float,
    panY: Float,
    onSelect: (String?) -> Unit,
    onMoveElement: (String, Float, Float) -> Unit,
    onResizeElement: ((String, Float, Float, Boolean, Boolean) -> Unit)? = null,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Stable state refs to avoid gesture restart on state change
    val currentZoom = remember { mutableFloatStateOf(zoom) }
    val currentPanX = remember { mutableFloatStateOf(panX) }
    val currentPanY = remember { mutableFloatStateOf(panY) }

    LaunchedEffect(zoom) { currentZoom.floatValue = zoom }
    LaunchedEffect(panX) { currentPanX.floatValue = panX }
    LaunchedEffect(panY) { currentPanY.floatValue = panY }

    Box(
        modifier = modifier
            .background(StudioColors.BackgroundCanvas)
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .drawBehind {
                drawCanvasGrid(canvasSize, currentPanX.floatValue, currentPanY.floatValue, currentZoom.floatValue)
            },
        contentAlignment = Alignment.TopStart
    ) {
        // Canvas content area with zoom/pan transform
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Tap to select - uses Unit key so it doesn't restart
                .pointerInput(Unit) {
                    val tapDensity = density
                    detectTapGestures { offset ->
                        val worldX = (offset.x - currentPanX.floatValue) / currentZoom.floatValue
                        val worldY = (offset.y - currentPanY.floatValue) / currentZoom.floatValue
                        val hit = hitTestElement(rootElement, worldX, worldY, canvasSize, tapDensity)
                        onSelect(hit?.id)
                    }
                }
                // Zoom + Pan - uses Unit key so it doesn't restart on state change
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        if (gestureZoom != 1f) {
                            val newZoom = (currentZoom.floatValue * gestureZoom).coerceIn(0.1f, 5f)
                            currentZoom.floatValue = newZoom
                            onZoomChange(newZoom)
                        }
                        if (pan.x != 0f || pan.y != 0f) {
                            currentPanX.floatValue += pan.x
                            currentPanY.floatValue += pan.y
                            onPanChange(currentPanX.floatValue, currentPanY.floatValue)
                        }
                    }
                }
                .graphicsLayer {
                    translationX = currentPanX.floatValue
                    translationY = currentPanY.floatValue
                    scaleX = currentZoom.floatValue
                    scaleY = currentZoom.floatValue
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            if (rootElement.visible) {
                CanvasElementRenderer(
                    element = rootElement,
                    selectedElementId = selectedElementId,
                    canvasSize = canvasSize,
                    onSelect = onSelect,
                    onMoveElement = onMoveElement
                )
            }
        }

        // Top-left info badges
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CanvasInfoBadge("Zoom: ${(zoom * 100).toInt()}%")
            CanvasInfoBadge("${countElements(rootElement)} instances")
        }

        // Resize handles on selected element (rendered in screen space)
        if (selectedElement != null && selectedElement.id != rootElement.id) {
            ResizeHandles(
                element = selectedElement,
                canvasSize = canvasSize,
                zoom = zoom,
                panX = panX,
                panY = panY,
                onResize = { dx, dy, changeW, changeH ->
                    onResizeElement?.invoke(selectedElement.id, dx, dy, changeW, changeH)
                }
            )
        }

        // Bottom-right zoom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(StudioColors.BackgroundDarker.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ZoomButton(Icons.Filled.ZoomOut) { onZoomChange((zoom - 0.1f).coerceAtLeast(0.1f)) }
            ZoomButton(Icons.Filled.ZoomIn) { onZoomChange((zoom + 0.1f).coerceAtMost(5f)) }
            ZoomButton(Icons.Filled.PanTool) { onZoomChange(1f); onPanChange(0f, 0f) }
        }
    }
}

@Composable
private fun ZoomButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
        Icon(icon, contentDescription = null, tint = StudioColors.TextTertiary, modifier = Modifier.size(14.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Resize Handles for selected element
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResizeHandles(
    element: GuiElement,
    canvasSize: Size,
    zoom: Float,
    panX: Float,
    panY: Float,
    onResize: (dx: Float, dy: Float, changeWidth: Boolean, changeHeight: Boolean) -> Unit
) {
    val density = LocalDensity.current
    val pos = element.prop("Position")?.value as? PropValue.UDim2Value ?: return
    val sz = element.prop("Size")?.value as? PropValue.UDim2Value ?: return

    val parentW = canvasSize.width
    val parentH = canvasSize.height

    val px = parentW * pos.xScale + with(density) { pos.xOffset.dp.toPx() }
    val py = parentH * pos.yScale + with(density) { pos.yOffset.dp.toPx() }
    val pw = (parentW * sz.xScale + with(density) { sz.xOffset.dp.toPx() }).coerceAtLeast(20f)
    val ph = (parentH * sz.yScale + with(density) { sz.yOffset.dp.toPx() }).coerceAtLeast(20f)

    // Convert to screen space with zoom/pan
    val screenX = px * zoom + panX
    val screenY = py * zoom + panY
    val screenW = pw * zoom
    val screenH = ph * zoom

    val handleSize = 10f
    val halfHandle = handleSize / 2f

    Box(
        modifier = Modifier
            .offset(
                x = with(density) { screenX.toDp() },
                y = with(density) { screenY.toDp() }
            )
            .size(
                width = with(density) { screenW.toDp() },
                height = with(density) { screenH.toDp() }
            )
    ) {
        // Selection border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(1.5.dp, StudioColors.CanvasSelection)
        )

        // Eight resize handles
        val handles = listOf(
            Triple(Offset(0f, 0f), true, true),
            Triple(Offset(screenW, 0f), true, false),
            Triple(Offset(0f, screenH), false, true),
            Triple(Offset(screenW, screenH), false, false),
            Triple(Offset(screenW / 2f, 0f), true, false),
            Triple(Offset(screenW, screenH / 2f), false, false),
            Triple(Offset(screenW / 2f, screenH), false, true),
            Triple(Offset(0f, screenH / 2f), true, true)
        )

        handles.forEach { (posOffset, changesWidth, changesHeight) ->
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (posOffset.x - halfHandle).toDp() },
                        y = with(density) { (posOffset.y - halfHandle).toDp() }
                    )
                    .size(with(density) { handleSize.toDp() })
                    .background(Color.White, CircleShape)
                    .border(1.dp, StudioColors.CanvasSelection, CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dx = if (changesWidth) dragAmount.x / density else 0f
                            val dy = if (changesHeight) dragAmount.y / density else 0f
                            onResize(dx, dy, changesWidth, changesHeight)
                        }
                    }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Element Renderer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CanvasElementRenderer(
    element: GuiElement,
    selectedElementId: String?,
    canvasSize: Size,
    onSelect: (String?) -> Unit = {},
    onMoveElement: (String, Float, Float) -> Unit = { _, _, _ -> },
    parentWidth: Float = canvasSize.width,
    parentHeight: Float = canvasSize.height
) {
    if (!element.visible) return

    val isSelected = element.id == selectedElementId
    val density = LocalDensity.current

    val elemPos = element.prop("Position")?.value as? PropValue.UDim2Value
    val elemSize = element.prop("Size")?.value as? PropValue.UDim2Value

    val pos = elemPos ?: PropValue.UDim2Value(0f, 0f, 0f, 0f)
    val size = elemSize ?: PropValue.UDim2Value(1f, 0f, 1f, 0f)

    val px = with(density) { (parentWidth * pos.xScale + pos.xOffset.dp.toPx()).toDp() }
    val py = with(density) { (parentHeight * pos.yScale + pos.yOffset.dp.toPx()).toDp() }
    val pw = with(density) { (parentWidth * size.xScale + size.xOffset.dp.toPx()).coerceAtLeast(4f).toDp() }
    val ph = with(density) { (parentHeight * size.yScale + size.yOffset.dp.toPx()).coerceAtLeast(4f).toDp() }

    Box(
        modifier = Modifier
            .offset(x = px, y = py)
            .size(pw, ph)
            .then(getBoxStyle(element))
            .clickable { onSelect(element.id) }
            .then(
                if (!element.locked) {
                    Modifier.pointerInput(element.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onMoveElement(element.id, dragAmount.x / density, dragAmount.y / density)
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
                    .border(1.5.dp, StudioColors.CanvasSelection.copy(alpha = 0.7f))
            )
        }

        // Empty container hint
        if (element.children.isEmpty() && element.type.canHaveChildren && element.type != ElementType.FRAME) {
            VanillaElementIcon(
                type = element.type,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(16.dp)
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
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(2.dp)
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
                        .size(16.dp)
                )
            }
            else -> {}
        }

        // Render children recursively
        if (element.children.isNotEmpty() && element.expanded) {
            element.children.forEach { child ->
                // Only render children if this is a container type
                if (element.type.canHaveChildren) {
                    CanvasElementRenderer(
                        element = child,
                        selectedElementId = selectedElementId,
                        canvasSize = canvasSize,
                        onSelect = onSelect,
                        onMoveElement = onMoveElement,
                        parentWidth = pw.value,
                        parentHeight = ph.value
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Box style from element properties
// ─────────────────────────────────────────────────────────────────────────────

private fun getBoxStyle(element: GuiElement): Modifier {
    var mod = Modifier

    val bgColor = element.prop("BackgroundColor3")?.value as? PropValue.ColorValue
    val bgTrans = element.prop("BackgroundTransparency")?.value as? PropValue.FloatValue
    val borderColor = element.prop("BorderColor3")?.value as? PropValue.ColorValue
    val borderSize = element.prop("BorderSizePixel")?.value as? PropValue.IntValue
    val visible = element.prop("Visible")?.value as? PropValue.BoolValue

    if (visible?.value == false) {
        mod = mod.alpha(0.3f)
    }

    if (bgColor != null) {
        val alpha = 1f - (bgTrans?.value ?: 0f)
        mod = mod.background(bgColor.value.copy(alpha = alpha.coerceIn(0f, 1f)))
    }

    if (borderColor != null && (borderSize?.value ?: 0) > 0) {
        mod = mod.border((borderSize!!.value).dp, borderColor.value)
    }

    return mod
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Grid
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawCanvasGrid(canvasSize: Size, panX: Float, panY: Float, zoom: Float) {
    val gridMinor = if (zoom >= 1.5f) 10f else if (zoom >= 0.8f) 20f else 40f
    val gridMajor = gridMinor * 5

    val right = canvasSize.width
    val bottom = canvasSize.height

    val gridOffsetX = (panX % gridMinor)
    val gridOffsetY = (panY % gridMinor)
    val gridMajorOffsetX = panX % gridMajor
    val gridMajorOffsetY = panY % gridMajor

    val strokeW = 0.5f

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
    val majorStrokeW = 1f
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
// Hit-test with bounds checking
// ─────────────────────────────────────────────────────────────────────────────

private fun hitTestElement(
    element: GuiElement,
    worldX: Float,
    worldY: Float,
    canvasSize: Size,
    density: Float
): GuiElement? {
    if (!element.visible) return null

    val elemPos = element.prop("Position")?.value as? PropValue.UDim2Value
    val elemSize = element.prop("Size")?.value as? PropValue.UDim2Value

    val pos = elemPos ?: PropValue.UDim2Value(0f, 0f, 0f, 0f)
    val sz = elemSize ?: PropValue.UDim2Value(1f, 0f, 1f, 0f)

    val parentW = canvasSize.width
    val parentH = canvasSize.height

    val px = parentW * pos.xScale + pos.xOffset * density
    val py = parentH * pos.yScale + pos.yOffset * density
    val pw = (parentW * sz.xScale + sz.xOffset * density).coerceAtLeast(0f)
    val ph = (parentH * sz.yScale + sz.yOffset * density).coerceAtLeast(0f)

    // Check children first (reverse order for top-most)
    for (child in element.children.reversed()) {
        val childCanvasSize = Size(pw, ph)
        val hit = hitTestElement(child, worldX - px, worldY - py, childCanvasSize, density)
        if (hit != null) return hit
    }

    // Check self
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
