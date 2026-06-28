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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.ui.components.*
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography
import com.robloxui.designer.viewmodel.EditorPanel
import com.robloxui.designer.viewmodel.EditorViewModel

/**
 * Main editor screen with the 3-panel Roblox Studio-inspired layout
 * optimized for mobile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel
) {
    val state = viewModel.state

    // Export dialog
    if (state.showExportDialog) {
        ExportDialog(
            rootElement = state.rootElement,
            onDismiss = { viewModel.setShowExportDialog(false) },
            onCopyCode = { /* clipboard already handled */ }
        )
    }

    Scaffold(
        topBar = {
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
        },
        bottomBar = {
            MobileBottomNav(
                activePanel = state.activePanel,
                onPanelChange = { viewModel.setActivePanel(it) },
                selectedElement = viewModel.selectedElement,
                onDelete = {
                    state.selectedElementId?.let { viewModel.deleteElement(it) }
                },
                onCopy = {
                    state.selectedElementId?.let { viewModel.copyElement(it) }
                },
                onPaste = {
                    viewModel.pasteElement()
                },
                hasClipboard = state.clipboard != null,
                hasSelection = state.selectedElementId != null
            )
        },
        containerColor = StudioColors.Background
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left sidebar: Toolbar (element palette)
            EditorToolbar(
                currentTool = state.tool,
                canUndo = state.undoStack.isNotEmpty(),
                canRedo = state.redoStack.isNotEmpty(),
                onToolChange = { viewModel.setTool(it) },
                onAddElement = { type ->
                    viewModel.addElement(type)
                },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onDelete = {
                    state.selectedElementId?.let { viewModel.deleteElement(it) }
                },
                onCopy = {
                    state.selectedElementId?.let { viewModel.copyElement(it) }
                },
                onPaste = { viewModel.pasteElement() },
                onExport = { viewModel.setShowExportDialog(true) },
                onPreview = { viewModel.togglePreview() },
                isPreviewing = state.previewMode,
                onNewProject = { viewModel.newProject() }
            )

            // Main content area
            Column(modifier = Modifier.weight(1f)) {
                if (state.previewMode) {
                    PreviewMode(viewModel = viewModel)
                } else {
                    // Canvas (always visible in edit mode)
                    CanvasView(
                        rootElement = state.rootElement,
                        selectedElementId = state.selectedElementId,
                        zoom = state.zoom,
                        panX = state.panOffsetX,
                        panY = state.panOffsetY,
                        onSelect = { viewModel.selectElement(it) },
                        onMoveElement = { _, _, _ -> },
                        onZoomChange = { viewModel.setZoom(it) },
                        onPanChange = { x, y -> viewModel.setPan(x, y) },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )

                    // Bottom panel (Explorer or Properties based on tab)
                    AnimatedVisibility(
                        visible = state.activePanel != EditorPanel.CANVAS,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        BottomEditorPanel(
                            activePanel = state.activePanel,
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        )
                    }
                }
            }
        }
    }
}

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
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App logo / name
            Icon(
                Icons.Filled.CropSquare,
                contentDescription = "Roblox UI Designer",
                tint = StudioColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "Roblox UI Designer",
                    style = StudioTypography.MonoLabel,
                    color = StudioColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    projectName,
                    style = StudioTypography.MonoSmall,
                    color = StudioColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Top bar actions
            TopBarIconButton(
                icon = Icons.Filled.Undo,
                label = "Undo",
                enabled = canUndo,
                onClick = onUndo
            )
            TopBarIconButton(
                icon = Icons.Filled.Redo,
                label = "Redo",
                enabled = canRedo,
                onClick = onRedo
            )

            Spacer(modifier = Modifier.width(4.dp))

            TopBarIconButton(
                icon = if (isPreviewing) Icons.Filled.Edit else Icons.Filled.PlayArrow,
                label = if (isPreviewing) "Edit" else "Preview",
                onClick = onPreview
            )
            TopBarIconButton(
                icon = Icons.Filled.FileDownload,
                label = "Export",
                onClick = onExport
            )
            TopBarIconButton(
                icon = Icons.Filled.AddBox,
                label = "New",
                onClick = onNewProject
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (enabled) StudioColors.ToolbarIcon else StudioColors.TextDisabled,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BottomEditorPanel(
    activePanel: EditorPanel,
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state

    Surface(
        modifier = modifier,
        color = StudioColors.Background,
        shadowElevation = 4.dp
    ) {
        when (activePanel) {
            EditorPanel.EXPLORER -> {
                DexExplorer(
                    rootElement = state.rootElement,
                    selectedElementId = state.selectedElementId,
                    flatElements = viewModel.flatElements,
                    onSelect = { viewModel.selectElement(it) },
                    onToggleExpand = { viewModel.toggleExpand(it) },
                    onDelete = { viewModel.deleteElement(it) },
                    onDuplicate = { viewModel.duplicateElement(it) },
                    onRename = { id, name -> viewModel.renameElement(id, name) },
                    onToggleVisibility = { id, vis -> viewModel.setVisibility(id, vis) },
                    onToggleLock = { id, lock -> viewModel.setLocked(id, lock) },
                    onCopy = { viewModel.copyElement(it) },
                    onAddElement = {
                        // Show element type picker
                        viewModel.addElement(ElementType.FRAME)
                    }
                )
            }
            EditorPanel.PROPERTIES -> {
                PropertiesPanel(
                    element = viewModel.selectedElement,
                    onPropertyChange = { elementId, propName, value ->
                        viewModel.updateProperty(elementId, propName, value)
                    }
                )
            }
            EditorPanel.TOOLBOX -> {
                ToolboxPanel(
                    onAddElement = { type -> viewModel.addElement(type) }
                )
            }
            else -> {}
        }
    }
}

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
    Surface(
        color = StudioColors.BackgroundDarker,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section tabs
            NavTab(
                icon = Icons.Filled.AccountTree,
                label = "Explorer",
                isActive = activePanel == EditorPanel.EXPLORER,
                onClick = { onPanelChange(EditorPanel.EXPLORER) }
            )
            NavTab(
                icon = Icons.Filled.Tune,
                label = "Properties",
                isActive = activePanel == EditorPanel.PROPERTIES,
                onClick = { onPanelChange(EditorPanel.PROPERTIES) },
                badge = if (selectedElement != null) "1" else null
            )
            NavTab(
                icon = Icons.Filled.Widgets,
                label = "Toolbox",
                isActive = activePanel == EditorPanel.TOOLBOX,
                onClick = { onPanelChange(EditorPanel.TOOLBOX) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            if (hasSelection) {
                SmallActionButton(
                    icon = Icons.Filled.ContentCopy,
                    label = "Copy",
                    onClick = onCopy
                )
                SmallActionButton(
                    icon = Icons.Filled.Delete,
                    label = "Delete",
                    onClick = onDelete
                )
            }
            if (hasClipboard) {
                SmallActionButton(
                    icon = Icons.Filled.ContentPaste,
                    label = "Paste",
                    onClick = onPaste
                )
            }
        }
    }
}

@Composable
private fun NavTab(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) StudioColors.Primary else StudioColors.ToolbarIcon,
                modifier = Modifier.size(22.dp)
            )
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(StudioColors.AccentRed, RoundedCornerShape(7.dp))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        badge,
                        style = StudioTypography.MonoSmall,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            style = StudioTypography.MonoSmall,
            color = if (isActive) StudioColors.Primary else StudioColors.TextTertiary
        )
    }
}

@Composable
private fun SmallActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = StudioColors.ToolbarIcon,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ToolboxPanel(
    onAddElement: (ElementType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioColors.Background)
            .padding(12.dp)
    ) {
        Text(
            "ELEMENTS",
            style = StudioTypography.MonoLabel,
            color = StudioColors.Primary,
            modifier = Modifier.padding(bottom = 8.dp)
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (categoryName, types) ->
                Text(
                    categoryName.uppercase(),
                    style = StudioTypography.MonoSmall,
                    color = StudioColors.TextTertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { type ->
                        ToolboxItem(
                            type = type,
                            onClick = { onAddElement(type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolboxItem(
    type: ElementType,
    onClick: () -> Unit
) {
    val iconData = getElementIcon(type)

    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioColors.Surface)
            .clickable { onClick() }
            .border(1.dp, StudioColors.SurfaceHighlight, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                iconData.icon,
                contentDescription = type.displayName,
                tint = iconData.color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                type.displayName,
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PreviewMode(
    viewModel: EditorViewModel
) {
    val state = viewModel.state
    var selectedElementId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioColors.BackgroundCanvas)
    ) {
        // Render a "PlayerGui" preview
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioColors.BackgroundDarker)
                .padding(16.dp)
        ) {
            // Device frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioColors.Background)
                    .border(
                        2.dp,
                        StudioColors.SurfaceHighlight,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                // Render elements in preview mode
                CanvasView(
                    rootElement = state.rootElement,
                    selectedElementId = selectedElementId,
                    zoom = state.zoom,
                    panX = state.panOffsetX,
                    panY = state.panOffsetY,
                    onSelect = { selectedElementId = it },
                    onMoveElement = { _, _, _ -> },
                    onZoomChange = { viewModel.setZoom(it) },
                    onPanChange = { x, y -> viewModel.setPan(x, y) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }

        // Preview mode badge
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .background(
                    StudioColors.AccentRed.copy(alpha = 0.9f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                "PREVIEW MODE",
                style = StudioTypography.MonoLabel,
                color = Color.White
            )
        }
    }
}
