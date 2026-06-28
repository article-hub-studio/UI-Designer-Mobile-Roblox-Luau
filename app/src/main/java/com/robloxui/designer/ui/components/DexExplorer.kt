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
import androidx.compose.ui.unit.sp
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * Compact Figma-style Dex Explorer for the right panel.
 */
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
    onToggleVisibility: ((String, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(StudioColors.ExplorerBg)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).background(StudioColors.BackgroundDarker).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "EXPLORER",
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "${countAll(rootElement)} items",
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                fontSize = 8.sp
            )
        }

        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp)

        // Element list
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            itemsIndexed(flatElements, key = { _, pair -> pair.first.id }) { _, (element, depth) ->
                ExplorerRow(
                    element = element,
                    depth = depth,
                    isSelected = element.id == selectedElementId,

                    onSelect = { onSelect(element.id) },
                    onToggleExpand = { onToggleExpand(element.id) },
                    onDelete = { onDelete(element.id) },
                    onDuplicate = { onDuplicate(element.id) },
                    onRename = { name -> onRename(element.id, name) }
                )
            }
        }
    }
}

@Composable
private fun ExplorerRow(
    element: GuiElement,
    depth: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(element.name) { mutableStateOf(element.name) }
    var showContextMenu by remember { mutableStateOf(false) }

    val bgColor = when {
        isSelected -> StudioColors.ExplorerItemSelected
        else -> Color.Transparent
    }

    // Context menu dropdown
    DropdownMenu(
        expanded = showContextMenu,
        onDismissRequest = { showContextMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Rename", style = StudioTypography.MonoSmall, color = StudioColors.TextPrimary, fontSize = 10.sp) },
            onClick = { showContextMenu = false; isEditing = true },
            leadingIcon = { Icon(Icons.Filled.Edit, null, tint = StudioColors.TextSecondary, modifier = Modifier.size(14.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Duplicate", style = StudioTypography.MonoSmall, color = StudioColors.TextPrimary, fontSize = 10.sp) },
            onClick = { showContextMenu = false; onDuplicate() },
            leadingIcon = { Icon(Icons.Filled.ContentCopy, null, tint = StudioColors.TextSecondary, modifier = Modifier.size(14.dp)) }
        )
        Divider(color = StudioColors.ToolbarDivider)
        DropdownMenuItem(
            text = { Text("Delete", style = StudioTypography.MonoSmall, color = StudioColors.AccentRed, fontSize = 10.sp) },
            onClick = { showContextMenu = false; onDelete() },
            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = StudioColors.AccentRed, modifier = Modifier.size(14.dp)) }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(bgColor)
            .clickable { onSelect() }
            .padding(start = (8 + depth * 14).dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expand/collapse arrow
        if (element.children.isNotEmpty()) {
            Icon(
                if (element.expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowRight,
                contentDescription = if (element.expanded) "Collapse" else "Expand",
                tint = StudioColors.TextTertiary,
                modifier = Modifier.size(14.dp).clickable { onToggleExpand() }
            )
        } else {
            Spacer(modifier = Modifier.width(14.dp))
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Element icon
        val iconData = getElementIcon(element.type)
        Icon(
            iconData.icon,
            contentDescription = element.type.displayName,
            tint = if (element.visible) iconData.color else StudioColors.TextDisabled,
            modifier = Modifier.size(12.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Element name
        if (isEditing) {
            var commit by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = editText,
                onValueChange = { editText = it },
                singleLine = true,
                textStyle = StudioTypography.MonoSmall.copy(
                    color = StudioColors.TextPrimary,
                    fontSize = 10.sp
                ),
                modifier = Modifier.weight(1f).height(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioColors.Primary,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = StudioColors.Primary,
                    focusedContainerColor = StudioColors.BackgroundInput,
                    unfocusedContainerColor = StudioColors.BackgroundInput
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        if (editText.isNotBlank()) {
                            onRename(editText)
                        }
                        isEditing = false
                    }
                )
            )
        } else {
            Text(
                element.name,
                style = StudioTypography.MonoText,
                color = if (element.visible) StudioColors.TextPrimary else StudioColors.TextDisabled,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        // Visibility toggle
        Icon(
            if (element.visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = if (element.visible) "Hide" else "Show",
            tint = if (element.visible) StudioColors.TextTertiary else StudioColors.TextDisabled,
            modifier = Modifier.size(12.dp).clickable { onToggleVisibility?.invoke(element.id, !element.visible) }
        )

        // Context menu trigger
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = "Menu",
            tint = StudioColors.TextTertiary,
            modifier = Modifier.size(12.dp).clickable { showContextMenu = true }
        )
    }
}

private fun countAll(element: GuiElement): Int {
    return 1 + element.children.sumOf { countAll(it) }
}
