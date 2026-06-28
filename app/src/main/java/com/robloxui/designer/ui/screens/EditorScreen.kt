package com.robloxui.designer.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.ui.components.*
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography
import com.robloxui.designer.viewmodel.EditorPanel
import com.robloxui.designer.viewmodel.EditorViewModel

/**
 * Figma-inspired landscape editor with compact panels.
 * Layout: Toolbar | Canvas | Right Panel (Explorer/Properties/Toolbox)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val state = viewModel.state

    if (state.showExportDialog) {
        ExportDialog(
            rootElement = state.rootElement,
            onDismiss = { viewModel.setShowExportDialog(false) },
            onCopyCode = {}
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(StudioColors.Background)) {
        // Top bar - compact Figma style
        EditorTopBar(
            projectName = state.rootElement.name,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            canUndo = state.undoStack.isNotEmpty(),
            canRedo = state.redoStack.isNotEmpty(),
            onExport = { viewModel.setShowExportDialog(true) },
            onPreview = { viewModel.togglePreview() },
            isPreviewing = state.previewMode,
            onNewProject = { viewModel.newProject() }
        )

        // Main area: Toolbar | Canvas | Right Panel
        Row(modifier = Modifier.fillMaxSize()) {
            // Left toolbar (compact)
            EditorToolbar(
                currentTool = state.tool,
                canUndo = state.undoStack.isNotEmpty(),
                canRedo = state.redoStack.isNotEmpty(),
                onToolChange = { viewModel.setTool(it) },
                onAddElement = { viewModel.addElement(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onDelete = { state.selectedElementId?.let { viewModel.deleteElement(it) } },
                onCopy = { state.selectedElementId?.let { viewModel.copyElement(it) } },
                onPaste = { viewModel.pasteElement() },
                onExport = { viewModel.setShowExportDialog(true) },
                onPreview = { viewModel.togglePreview() },
                isPreviewing = state.previewMode,
                onNewProject = { viewModel.newProject() }
            )

            // Center: Canvas area
            Column(modifier = Modifier.weight(1f)) {
                if (state.previewMode) {
                    PreviewMode(viewModel = viewModel)
                } else {
                    CanvasView(
                        rootElement = state.rootElement,
                        selectedElementId = state.selectedElementId,
                        zoom = state.zoom,
                        panX = state.panOffsetX,
                        panY = state.panOffsetY,
                        onSelect = { viewModel.selectElement(it) },
                        onMoveElement = { id, dx, dy -> viewModel.moveElementByDelta(id, dx, dy) },
                        onZoomChange = { viewModel.setZoom(it) },
                        onPanChange = { x, y -> viewModel.setPan(x, y) },
                        modifier = Modifier.fillMaxSize().weight(1f)
                    )
                }
            }

            // Right panel: Tabbed Explorer/Properties/Toolbox
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(StudioColors.BackgroundPanel)
            ) {
                // Compact tab bar
                RightPanelTabs(
                    activePanel = state.activePanel,
                    onPanelChange = { viewModel.setActivePanel(it) },
                    selectedElement = viewModel.selectedElement
                )

                Divider(
                    color = StudioColors.ToolbarDivider,
                    thickness = 1.dp
                )

                // Panel content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (state.activePanel) {
                        EditorPanel.EXPLORER -> {
                            val flatList = remember(state.rootElement) {
                                buildFlatList(state.rootElement)
                            }
                            DexExplorer(
                                rootElement = state.rootElement,
                                selectedElementId = state.selectedElementId,
                                flatElements = flatList,
                                onSelect = { viewModel.selectElement(it) },
                                onToggleExpand = { viewModel.toggleExpand(it) },
                                onDelete = { viewModel.deleteElement(it) },
                                onDuplicate = { viewModel.duplicateElement(it) },
                                onRename = { id, name -> viewModel.renameElement(id, name) }
                            )
                        }
                        EditorPanel.PROPERTIES -> {
                            PropertiesPanel(
                                element = viewModel.selectedElement,
                                onPropertyChange = { id, prop, value ->
                                    viewModel.updateProperty(id, prop, value)
                                }
                            )
                        }
                        EditorPanel.TOOLBOX -> {
                            ToolboxPanel(
                                onAddElement = { viewModel.addElement(it) }
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun RightPanelTabs(
    activePanel: EditorPanel,
    onPanelChange: (EditorPanel) -> Unit,
    selectedElement: GuiElement?
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp).background(StudioColors.BackgroundDarker),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            EditorPanel.EXPLORER to Icons.Filled.AccountTree,
            EditorPanel.PROPERTIES to Icons.Filled.Tune,
            EditorPanel.TOOLBOX to Icons.Filled.Widgets
        ).forEach { (panel, icon) ->
            val isActive = activePanel == panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onPanelChange(panel) }
                    .background(if (isActive) StudioColors.BackgroundPanel else StudioColors.BackgroundDarker),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isActive) StudioColors.Primary else StudioColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (panel) {
                            EditorPanel.EXPLORER -> "Explorer"
                            EditorPanel.PROPERTIES -> "Properties"
                            EditorPanel.TOOLBOX -> "Toolbox"
                            else -> ""
                        },
                        style = StudioTypography.MonoSmall,
                        color = if (isActive) StudioColors.Primary else StudioColors.TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    projectName: String,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onExport: () -> Unit,
    onPreview: () -> Unit,
    isPreviewing: Boolean,
    onNewProject: () -> Unit
) {
    Surface(
        color = StudioColors.BackgroundDarker,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(32.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Code,
                contentDescription = null,
                tint = StudioColors.Primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                projectName,
                style = StudioTypography.MonoLabel,
                color = StudioColors.TextPrimary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Action icons
            listOf(
                Icons.Filled.AddBox to "New" to onNewProject,
                Icons.Filled.Undo to "Undo" to onUndo,
                Icons.Filled.Redo to "Redo" to onRedo,
                (if (isPreviewing) Icons.Filled.Edit else Icons.Filled.PlayArrow) to
                    (if (isPreviewing) "Edit" else "Preview") to onPreview,
                Icons.Filled.FileDownload to "Export" to onExport
            ).forEach { (iconLabel, onClick) ->
                val (icon, label) = iconLabel
                val enabled = when (label) {
                    "Undo" -> canUndo
                    "Redo" -> canRedo
                    else -> true
                }
                IconButton(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = if (enabled) StudioColors.ToolbarIcon else StudioColors.TextDisabled,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// --- Bottom Panel (kept for backward compatibility) ---

@Composable
private fun BottomEditorPanel(
    activePanel: EditorPanel,
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(StudioColors.BackgroundPanel)) {
        when (activePanel) {
            EditorPanel.EXPLORER -> {
                val flatList = remember(viewModel.state.rootElement) {
                    buildFlatList(viewModel.state.rootElement)
                }
                DexExplorer(
                    rootElement = viewModel.state.rootElement,
                    selectedElementId = viewModel.state.selectedElementId,
                    flatElements = flatList,
                    onSelect = { viewModel.selectElement(it) },
                    onToggleExpand = { viewModel.toggleExpand(it) },
                    onDelete = { viewModel.deleteElement(it) },
                    onDuplicate = { viewModel.duplicateElement(it) },
                    onRename = { id, name -> viewModel.renameElement(id, name) }
                )
            }
            EditorPanel.PROPERTIES -> {
                PropertiesPanel(
                    element = viewModel.selectedElement,
                    onPropertyChange = { id, prop, value ->
                        viewModel.updateProperty(id, prop, value)
                    }
                )
            }
            EditorPanel.TOOLBOX -> {
                ToolboxPanel(
                    onAddElement = { viewModel.addElement(it) }
                )
            }
            else -> {}
        }
    }
}

private fun buildFlatList(element: GuiElement): List<Pair<GuiElement, Int>> {
    val result = mutableListOf<Pair<GuiElement, Int>>()
    fun walk(elem: GuiElement, depth: Int) {
        result.add(elem to depth)
        if (elem.expanded) {
            elem.children.forEach { walk(it, depth + 1) }
        }
    }
    walk(element, 0)
    return result
}

// --- Toolbox Panel (compact) ---

@Composable
private fun ToolboxPanel(onAddElement: (ElementType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(StudioColors.BackgroundPanel).padding(8.dp)
    ) {
        Text(
            "ELEMENTS",
            style = StudioTypography.MonoSmall,
            color = StudioColors.Primary,
            fontSize = 9.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val categories = listOf(
            "Containers" to listOf(ElementType.FRAME, ElementType.SCOPING_FRAME, ElementType.CANVAS),
            "Buttons" to listOf(ElementType.BUTTON, ElementType.IMAGE_BUTTON),
            "Labels" to listOf(ElementType.TEXT_LABEL, ElementType.TEXT_BOX),
            "Visual" to listOf(ElementType.IMAGE_LABEL, ElementType.VIEWPORT_FRAME, ElementType.VIDEO_FRAME),
            "Layout" to listOf(ElementType.UI_LIST_LAYOUT, ElementType.UI_GRID_LAYOUT, ElementType.UI_PADDING),
            "Decoration" to listOf(ElementType.UI_CORNER, ElementType.UI_STROKE, ElementType.UI_GRADIENT)
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { (categoryName, types) ->
                Text(
                    categoryName.uppercase(),
                    style = StudioTypography.MonoSmall,
                    color = StudioColors.TextTertiary,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { type ->
                        ToolboxItem(type = type, onClick = { onAddElement(type) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolboxItem(type: ElementType, onClick: () -> Unit) {
    val iconData = getElementIcon(type)
    Box(
        modifier = Modifier
            .size(width = 54.dp, height = 44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(StudioColors.Surface)
            .clickable { onClick() }
            .border(1.dp, StudioColors.SurfaceHighlight, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                iconData.icon,
                contentDescription = type.displayName,
                tint = iconData.color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                type.displayName,
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextSecondary,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Preview Mode ---

@Composable
private fun PreviewMode(viewModel: EditorViewModel) {
    val state = viewModel.state
    var selectedElementId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(StudioColors.BackgroundCanvas)) {
        Column(
            modifier = Modifier.fillMaxSize().background(StudioColors.BackgroundDarker).padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioColors.Background)
                    .border(2.dp, StudioColors.SurfaceHighlight, RoundedCornerShape(12.dp))
            ) {
                CanvasView(
                    rootElement = state.rootElement,
                    selectedElementId = selectedElementId,
                    zoom = state.zoom,
                    panX = state.panOffsetX,
                    panY = state.panOffsetY,
                    onSelect = { selectedElementId = it },
                    onMoveElement = { id, dx, dy -> viewModel.moveElementByDelta(id, dx, dy) },
                    onZoomChange = { viewModel.setZoom(it) },
                    onPanChange = { x, y -> viewModel.setPan(x, y) },
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
                .background(StudioColors.AccentRed.copy(alpha = 0.9f), RoundedCornerShape(3.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "PREVIEW",
                style = StudioTypography.MonoLabel,
                color = Color.White,
                fontSize = 9.sp
            )
        }
    }
}

// Keep MobileBottomNav for backward compatibility
@Composable
private fun MobileBottomNav(
    activePanel: EditorPanel,
    onPanelChange: (EditorPanel) -> Unit,
    selectedElement: GuiElement?,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    hasClipboard: Boolean,
    hasSelection: Boolean
) {
    Surface(color = StudioColors.BackgroundDarker, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = mapOf(
                EditorPanel.EXPLORER to (Icons.Filled.AccountTree to "Explorer"),
                EditorPanel.PROPERTIES to (Icons.Filled.Tune to "Properties"),
                EditorPanel.TOOLBOX to (Icons.Filled.Widgets to "Toolbox")
            )
            tabs.forEach { (panel, pair) ->
                val (icon, label) = pair
                NavTab(
                    icon = icon,
                    label = label,
                    isActive = activePanel == panel,
                    badge = if (panel == EditorPanel.PROPERTIES && selectedElement != null) "1" else null,
                    onClick = { onPanelChange(panel) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (hasSelection) {
                SmallActionButton(Icons.Filled.ContentCopy, "Copy", onClick = onCopy)
                SmallActionButton(Icons.Filled.Delete, "Delete", onClick = onDelete)
            }
            if (hasClipboard) {
                SmallActionButton(Icons.Filled.ContentPaste, "Paste", onClick = onPaste)
            }
        }
    }
}

@Composable
private fun NavTab(icon: ImageVector, label: String, isActive: Boolean, badge: String? = null, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp).clickable { onClick() }.padding(vertical = 2.dp)
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = if (isActive) StudioColors.Primary else StudioColors.ToolbarIcon, modifier = Modifier.size(18.dp))
            if (badge != null) {
                Box(modifier = Modifier.size(12.dp).background(StudioColors.AccentRed, RoundedCornerShape(6.dp)).align(Alignment.TopEnd), contentAlignment = Alignment.Center) {
                    Text(badge, style = StudioTypography.MonoSmall, color = Color.White, fontSize = 8.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(label, style = StudioTypography.MonoSmall, color = if (isActive) StudioColors.Primary else StudioColors.TextTertiary, fontSize = 9.sp)
    }
}

@Composable
private fun SmallActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(icon, contentDescription = label, tint = StudioColors.ToolbarIcon, modifier = Modifier.size(16.dp))
    }
}
