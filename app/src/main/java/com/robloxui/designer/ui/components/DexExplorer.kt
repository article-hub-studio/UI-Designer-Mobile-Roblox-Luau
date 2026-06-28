package com.robloxui.designer.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

@Composable
fun DexExplorer(
    rootElement: GuiElement,
    selectedElementId: String?,
    flatElements: List<Pair<GuiElement, Int>>,
    onSelect: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onToggleVisibility: (String, Boolean) -> Unit,
    onToggleLock: (String, Boolean) -> Unit,
    onCopy: (String) -> Unit,
    onAddElement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    var contextMenuElementId by remember { mutableStateOf<String?>(null) }
    var renamingId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    Column(modifier = modifier.background(StudioColors.ExplorerBg)) {
        // Header
        ExplorerHeader(
            elementCount = flatElements.size,
            onAddElement = onAddElement
        )

        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp)

        // Tree
        if (flatElements.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No elements yet.\nTap + to add one.",
                    style = StudioTypography.MonoText,
                    color = StudioColors.TextDisabled
                )
            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                itemsIndexed(
                    items = flatElements,
                    key = { _, (el, _) -> el.id }
                ) { _, (element, depth) ->
                    ExplorerTreeItem(
                        element = element,
                        depth = depth,
                        isSelected = element.id == selectedElementId,
                        isRenaming = element.id == renamingId,
                        renameText = renameText,
                        onSelect = { onSelect(element.id) },
                        onToggleExpand = { onToggleExpand(element.id) },
                        onRenameStart = {
                            renamingId = element.id
                            renameText = element.name
                        },
                        onRenameConfirm = {
                            if (renameText.isNotBlank()) {
                                onRename(element.id, renameText)
                            }
                            renamingId = null
                        },
                        onRenameCancel = { renamingId = null },
                        onRenameTextChange = { renameText = it },
                        onDelete = { onDelete(element.id) },
                        onDuplicate = { onDuplicate(element.id) },
                        onCopy = { onCopy(element.id) },
                        onToggleVisibility = { onToggleVisibility(element.id, !element.visible) },
                        onToggleLock = { onToggleLock(element.id, !element.locked) },
                        showContextMenu = element.id == contextMenuElementId,
                        onContextMenu = { contextMenuElementId = element.id },
                        onDismissContextMenu = { contextMenuElementId = null }
                    )
                }
            }
        }

        // Bottom status
        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp)
        ExplorerStatusBar(count = flatElements.size)
    }
}

@Composable
private fun ExplorerHeader(
    elementCount: Int,
    onAddElement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioColors.BackgroundDarker)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.AccountTree,
            contentDescription = "Explorer",
            tint = StudioColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Explorer",
            style = StudioTypography.MonoLabel,
            color = StudioColors.TextSecondary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "$elementCount",
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onAddElement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Element",
                tint = StudioColors.Primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ExplorerStatusBar(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioColors.BackgroundDarker)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "GUI Instances: $count",
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExplorerTreeItem(
    element: GuiElement,
    depth: Int,
    isSelected: Boolean,
    isRenaming: Boolean,
    renameText: String,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit,
    onRenameStart: () -> Unit,
    onRenameConfirm: () -> Unit,
    onRenameCancel: () -> Unit,
    onRenameTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCopy: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    showContextMenu: Boolean,
    onContextMenu: () -> Unit,
    onDismissContextMenu: () -> Unit
) {
    val bgColor = when {
        isSelected -> StudioColors.ExplorerItemSelected
        else -> Color.Transparent
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .combinedClickable(
                    onClick = onSelect,
                    onLongClick = onContextMenu
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onSelect() },
                        onLongPress = { onContextMenu() }
                    )
                }
                .padding(start = (12 + depth * 16).dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/collapse arrow
            if (element.children.isNotEmpty()) {
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (element.expanded)
                            Icons.Filled.ArrowDropDown
                        else
                            Icons.Filled.ArrowRight,
                        contentDescription = if (element.expanded) "Collapse" else "Expand",
                        tint = StudioColors.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Element type icon
            ElementTypeIcon(
                type = element.type,
                size = 16
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Element name
            if (isRenaming) {
                var text by remember(element.id) { mutableStateOf(element.name) }
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onRenameTextChange(it)
                    },
                    singleLine = true,
                    textStyle = StudioTypography.MonoText.copy(
                        color = StudioColors.TextPrimary
                    ),
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioColors.Primary,
                        unfocusedBorderColor = StudioColors.PropInputBorder,
                        cursorColor = StudioColors.Primary,
                        focusedContainerColor = StudioColors.BackgroundInput,
                        unfocusedContainerColor = StudioColors.BackgroundInput
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { onRenameConfirm() }
                    )
                )
            } else {
                Text(
                    text = element.name,
                    style = StudioTypography.MonoText,
                    color = if (isSelected) StudioColors.TextPrimary
                    else StudioColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Visibility and lock toggles
            if (element.id != "root") {
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (element.visible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = if (element.visible) "Hide" else "Show",
                        tint = if (element.visible) StudioColors.TextTertiary
                        else StudioColors.TextDisabled,
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (element.locked) Icons.Filled.Lock
                        else Icons.Filled.LockOpen,
                        contentDescription = if (element.locked) "Unlock" else "Lock",
                        tint = if (element.locked) StudioColors.AccentOrange
                        else StudioColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Context menu dropdown
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = onDismissContextMenu,
            modifier = Modifier.background(StudioColors.Surface)
        ) {
            DropdownMenuItem(
                text = { Text("Rename", style = StudioTypography.MonoText, color = StudioColors.TextPrimary) },
                onClick = { onDismissContextMenu(); onRenameStart() },
                leadingIcon = { Icon(Icons.Filled.Edit, null, tint = StudioColors.TextSecondary, modifier = Modifier.size(16.dp)) }
            )
            DropdownMenuItem(
                text = { Text("Copy", style = StudioTypography.MonoText, color = StudioColors.TextPrimary) },
                onClick = { onDismissContextMenu(); onCopy() },
                leadingIcon = { Icon(Icons.Filled.ContentCopy, null, tint = StudioColors.TextSecondary, modifier = Modifier.size(16.dp)) }
            )
            DropdownMenuItem(
                text = { Text("Duplicate", style = StudioTypography.MonoText, color = StudioColors.TextPrimary) },
                onClick = { onDismissContextMenu(); onDuplicate() },
                leadingIcon = { Icon(Icons.Filled.ContentCopy, null, tint = StudioColors.TextSecondary, modifier = Modifier.size(16.dp)) }
            )
            HorizontalDivider(color = StudioColors.ToolbarDivider)
            DropdownMenuItem(
                text = { Text("Delete", style = StudioTypography.MonoText, color = StudioColors.AccentRed) },
                onClick = { onDismissContextMenu(); onDelete() },
                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = StudioColors.AccentRed, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}
