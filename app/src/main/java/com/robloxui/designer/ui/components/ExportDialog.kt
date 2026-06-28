package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.model.PropValue
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * Export dialog showing generated Luau code with copy and share options.
 * Features: Live code preview, element tree preview, stats, copy-to-clipboard.
 */
@Composable
fun ExportDialog(
    rootElement: GuiElement,
    onDismiss: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val luauCode = remember(rootElement) { generateLuauExport(rootElement) }
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioColors.DialogBg,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Code,
                    contentDescription = null,
                    tint = StudioColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Export to Luau",
                    style = StudioTypography.MonoLabel,
                    color = StudioColors.TextPrimary
                )
            }
        },
        text = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = StudioColors.BackgroundDarker,
                    contentColor = StudioColors.Primary,
                    divider = { Divider(color = StudioColors.ToolbarDivider) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text("Luau Code", style = StudioTypography.MonoSmall, color = StudioColors.TextSecondary)
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text("Preview", style = StudioTypography.MonoSmall, color = StudioColors.TextSecondary)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Code view with monospace font
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioColors.BackgroundDarker)
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = luauCode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = TextUnit(11f, TextUnitType.Sp),
                                    lineHeight = TextUnit(16f, TextUnitType.Sp),
                                    color = StudioColors.TextPrimary
                                )
                            }
                        }
                    }
                    1 -> {
                        // Preview of element tree structure
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioColors.BackgroundCanvas)
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            ExportTreePreview(element = rootElement, depth = 0)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val stats = countExportStats(rootElement)
                    StatBadge("Elements", "${stats.first}")
                    StatBadge("Lines", "${stats.second}")
                    StatBadge("Parents", "${stats.third}")
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StudioColors.TextSecondary
                    ),
                    border = BorderStroke(1.dp, StudioColors.PropInputBorder)
                ) {
                    Text("Close", style = StudioTypography.MonoSmall)
                }

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(luauCode))
                        copied = true
                        onCopyCode(luauCode)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioColors.Primary,
                        contentColor = StudioColors.OnPrimary
                    )
                ) {
                    Icon(
                        if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (copied) "Copied!" else "Copy Code",
                        style = StudioTypography.MonoSmall
                    )
                }
            }
        }
    )
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = StudioTypography.MonoLabel,
            color = StudioColors.Primary
        )
        Text(
            label,
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary
        )
    }
}

@Composable
private fun ExportTreePreview(element: GuiElement, depth: Int) {
    val indent = "  ".repeat(depth)
    val icon = "\u25A0"

    Text(
        text = "$indent$icon ${element.type.className}: \"${element.name}\"",
        fontFamily = FontFamily.Monospace,
        fontSize = TextUnit(11f, TextUnitType.Sp),
        color = StudioColors.TextPrimary
    )

    element.children.forEach { child ->
        ExportTreePreview(element = child, depth = depth + 1)
    }
}

private fun countExportStats(element: GuiElement): Triple<Int, Int, Int> {
    val totalElements = 1 + element.children.sumOf { countExportStats(it).first }
    val lines = 6 + element.children.sumOf { countExportStats(it).second }
    val parents = if (element.children.isNotEmpty()) 1 + element.children.sumOf { countExportStats(it).third } else 0
    return Triple(totalElements, lines, parents)
}

/**
 * Generate complete Luau export code from the element tree.
 */
private fun generateLuauExport(element: GuiElement): String {
    val sb = StringBuilder()
    sb.appendLine("--[[")
    sb.appendLine("  Roblox UI Designer - Exported Layout")
    sb.appendLine("  Open Source Mobile UI Designer for Roblox")
    sb.appendLine("  Total Instances: ${countAllElements(element)}")
    sb.appendLine("--]]")
    sb.appendLine()
    sb.appendLine("local Players = game:GetService(\"Players\")")
    sb.appendLine()
    sb.appendLine("-- Create ScreenGui")
    sb.appendLine("local screenGui = Instance.new(\"ScreenGui\")")
    sb.appendLine("screenGui.Name = \"${element.name}\"")
    sb.appendLine("screenGui.ResetOnSpawn = false")
    sb.appendLine()
    sb.appendLine("-- Build UI Hierarchy")
    sb.appendLine()

    fun writeInstance(el: GuiElement, parentVar: String, varName: String) {
        sb.appendLine("-- $varName: ${el.type.displayName}")
        sb.appendLine("local $varName = Instance.new(\"${el.type.className}\")")
        sb.appendLine("$varName.Name = \"${el.name}\"")

        // Position
        val pos = el.prop("Position")?.value as? PropValue.UDim2Value
        if (pos != null) {
            sb.appendLine("$varName.Position = UDim2.new(${pos.xScale}, ${pos.xOffset}, ${pos.yScale}, ${pos.yOffset})")
        }

        // Size
        val size = el.prop("Size")?.value as? PropValue.UDim2Value
        if (size != null) {
            sb.appendLine("$varName.Size = UDim2.new(${size.xScale}, ${size.xOffset}, ${size.yScale}, ${size.yOffset})")
        }

        // AnchorPoint
        val anchor = el.prop("AnchorPoint")?.value as? PropValue.Vector2Value
        if (anchor != null) {
            sb.appendLine("$varName.AnchorPoint = Vector2.new(${anchor.x}, ${anchor.y})")
        }

        // Background
        val bgColor = el.prop("BackgroundColor3")?.value as? PropValue.ColorValue
        if (bgColor != null) {
            val r = (bgColor.value.red * 255).toInt()
            val g = (bgColor.value.green * 255).toInt()
            val b = (bgColor.value.blue * 255).toInt()
            sb.appendLine("$varName.BackgroundColor3 = Color3.fromRGB($r, $g, $b)")
        }

        val bgTrans = el.prop("BackgroundTransparency")?.value as? PropValue.FloatValue
        if (bgTrans != null && bgTrans.value > 0.01f) {
            sb.appendLine("$varName.BackgroundTransparency = ${bgTrans.value}")
        }

        // Border
        val borderSize = el.prop("BorderSizePixel")?.value as? PropValue.IntValue
        if (borderSize != null && borderSize.value > 0) {
            sb.appendLine("$varName.BorderSizePixel = ${borderSize.value}")
        }
        val borderColor = el.prop("BorderColor3")?.value as? PropValue.ColorValue
        if (borderColor != null) {
            val r = (borderColor.value.red * 255).toInt()
            val g = (borderColor.value.green * 255).toInt()
            val b = (borderColor.value.blue * 255).toInt()
            sb.appendLine("$varName.BorderColor3 = Color3.fromRGB($r, $g, $b)")
        }

        // Rotation & Visibility
        val rotation = el.prop("Rotation")?.value as? PropValue.FloatValue
        if (rotation != null && rotation.value != 0f) {
            sb.appendLine("$varName.Rotation = ${rotation.value}")
        }
        val visible = el.prop("Visible")?.value as? PropValue.BoolValue
        if (visible != null && !visible.value) {
            sb.appendLine("$varName.Visible = false")
        }
        val zIndex = el.prop("ZIndex")?.value as? PropValue.IntValue
        if (zIndex != null && zIndex.value != 1) {
            sb.appendLine("$varName.ZIndex = ${zIndex.value}")
        }

        // Text
        val text = el.prop("Text")?.value as? PropValue.StringValue
        if (text != null) {
            sb.appendLine("$varName.Text = \"${escapeLuau(text.value)}\"")
        }
        val textColor = el.prop("TextColor3")?.value as? PropValue.ColorValue
        if (textColor != null) {
            val r = (textColor.value.red * 255).toInt()
            val g = (textColor.value.green * 255).toInt()
            val b = (textColor.value.blue * 255).toInt()
            sb.appendLine("$varName.TextColor3 = Color3.fromRGB($r, $g, $b)")
        }
        val textSize = el.prop("TextSize")?.value as? PropValue.IntValue
        if (textSize != null) {
            sb.appendLine("$varName.TextSize = ${textSize.value}")
        }
        val font = el.prop("Font")?.value as? PropValue.EnumValue
        if (font != null) {
            sb.appendLine("$varName.Font = Enum.Font.${font.value}")
        }
        val textX = el.prop("TextXAlignment")?.value as? PropValue.EnumValue
        if (textX != null) {
            sb.appendLine("$varName.TextXAlignment = Enum.TextXAlignment.${textX.value}")
        }
        val textY = el.prop("TextYAlignment")?.value as? PropValue.EnumValue
        if (textY != null) {
            sb.appendLine("$varName.TextYAlignment = Enum.TextYAlignment.${textY.value}")
        }
        val textScaled = el.prop("TextScaled")?.value as? PropValue.BoolValue
        if (textScaled != null && textScaled.value) {
            sb.appendLine("$varName.TextScaled = true")
        }

        // ScrollingFrame
        val scrollDir = el.prop("ScrollingDirection")?.value as? PropValue.EnumValue
        if (scrollDir != null) {
            sb.appendLine("$varName.ScrollingDirection = Enum.ScrollingDirection.${scrollDir.value}")
        }
        val scrollThick = el.prop("ScrollBarThickness")?.value as? PropValue.IntValue
        if (scrollThick != null) {
            sb.appendLine("$varName.ScrollBarThickness = ${scrollThick.value}")
        }
        val canvasSize = el.prop("CanvasSize")?.value as? PropValue.UDim2Value
        if (canvasSize != null) {
            sb.appendLine("$varName.CanvasSize = UDim2.new(${canvasSize.xScale}, ${canvasSize.xOffset}, ${canvasSize.yScale}, ${canvasSize.yOffset})")
        }

        // Image
        val image = el.prop("Image")?.value as? PropValue.StringValue
        if (image != null && image.value.isNotEmpty() && image.value != "rbxassetid://0") {
            sb.appendLine("$varName.Image = \"${escapeLuau(image.value)}\"")
        }

        // Layout specifics
        if (el.type == com.robloxui.designer.model.ElementType.UI_LIST_LAYOUT) {
            val direction = el.prop("FillDirection")?.value as? PropValue.EnumValue
            if (direction != null) sb.appendLine("$varName.FillDirection = Enum.FillDirection.${direction.value}")
            val hAlign = el.prop("HorizontalAlignment")?.value as? PropValue.EnumValue
            if (hAlign != null) sb.appendLine("$varName.HorizontalAlignment = Enum.HorizontalAlignment.${hAlign.value}")
            val vAlign = el.prop("VerticalAlignment")?.value as? PropValue.EnumValue
            if (vAlign != null) sb.appendLine("$varName.VerticalAlignment = Enum.VerticalAlignment.${vAlign.value}")
            val padding = el.prop("Padding")?.value as? PropValue.UDim2Value
            if (padding != null) sb.appendLine("$varName.Padding = UDim.new(${padding.xScale}, ${padding.xOffset})")
        }

        if (el.type == com.robloxui.designer.model.ElementType.UI_CORNER) {
            val cr = el.prop("CornerRadius")?.value as? PropValue.UDim2Value
            if (cr != null) sb.appendLine("$varName.CornerRadius = UDim.new(${cr.xScale}, ${cr.xOffset})")
        }

        val clips = el.prop("ClipsDescendants")?.value as? PropValue.BoolValue
        if (clips != null && clips.value) {
            sb.appendLine("$varName.ClipsDescendants = true")
        }

        sb.appendLine("$varName.Parent = $parentVar")
        sb.appendLine()

        // Children
        el.children.forEachIndexed { i, child ->
            val childVarName = "${varName}_${sanitizeVarName(child.name)}"
            writeInstance(child, varName, childVarName)
        }
    }

    element.children.forEachIndexed { i, child ->
        val childVarName = "gui_${sanitizeVarName(child.name)}"
        writeInstance(child, "screenGui", childVarName)
    }

    sb.appendLine("-- Parent to PlayerGui")
    sb.appendLine("screenGui.Parent = Players.LocalPlayer:WaitForChild(\"PlayerGui\")")
    sb.appendLine()
    sb.appendLine("return screenGui")

    return sb.toString()
}

private fun sanitizeVarName(name: String): String {
    return name
        .replace(" ", "_")
        .replace("-", "_")
        .replace(".", "_")
        .replace("(", "_")
        .replace(")", "_")
        .replace("'", "")
        .replace("\"", "")
        .ifEmpty { "unnamed" }
}

private fun escapeLuau(s: String): String {
    return s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

private fun countAllElements(element: GuiElement): Int {
    return 1 + element.children.sumOf { countAllElements(it) }
}
