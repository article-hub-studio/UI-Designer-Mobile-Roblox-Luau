package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robloxui.designer.model.*
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

/**
 * Compact Figma-style properties panel.
 */
@Composable
fun PropertiesPanel(
    element: GuiElement?,
    onPropertyChange: (String, String, PropValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(StudioColors.PropBg)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).background(StudioColors.BackgroundDarker).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (element != null) "PROPERTIES - ${element.name}" else "PROPERTIES",
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp)

        if (element == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Select an element",
                    style = StudioTypography.MonoSmall,
                    color = StudioColors.TextDisabled,
                    fontSize = 10.sp
                )
            }
        } else {
            PropertiesContent(element = element, onPropertyChange = onPropertyChange)
        }
    }
}

@Composable
private fun PropertiesHeader(element: GuiElement?) {
    Row(
        modifier = Modifier.fillMaxWidth().background(StudioColors.PropCategoryBg).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (element != null) {
            val iconData = getElementIcon(element.type)
            Icon(iconData.icon, null, tint = iconData.color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                element.name,
                style = StudioTypography.MonoText,
                color = StudioColors.TextPrimary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                element.type.displayName,
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun PropertyCategory(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(StudioColors.PropCategoryBg).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            style = StudioTypography.MonoLabel,
            color = StudioColors.TextTertiary,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun PropertyRow(
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(StudioColors.PropRowBg)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = StudioTypography.MonoSmall,
            color = StudioColors.PropLabelText,
            fontSize = 9.sp,
            modifier = Modifier.width(64.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun PropertyValueEditor(
    prop: Property,
    onValueChange: (PropValue) -> Unit
) {
    val value = prop.value
    when (value) {
        is PropValue.StringValue -> {
            var text by remember(value) { mutableStateOf(value.value) }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText, fontSize = 9.sp),
                modifier = Modifier.fillMaxWidth().height(22.dp),
                colors = TextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onValueChange(PropValue.StringValue(text)) }
                )
            )
        }
        is PropValue.FloatValue -> {
            var text by remember(value) { mutableStateOf(String.format("%.2f", value.value)) }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText, fontSize = 9.sp),
                modifier = Modifier.fillMaxWidth().height(22.dp),
                colors = TextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsed = text.toFloatOrNull()
                        if (parsed != null) onValueChange(PropValue.FloatValue(parsed))
                    }
                )
            )
        }
        is PropValue.IntValue -> {
            var text by remember(value) { mutableStateOf(value.value.toString()) }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText, fontSize = 9.sp),
                modifier = Modifier.fillMaxWidth().height(22.dp),
                colors = TextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsed = text.toIntOrNull()
                        if (parsed != null) onValueChange(PropValue.IntValue(parsed))
                    }
                )
            )
        }
        is PropValue.BoolValue -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = value.value,
                    onCheckedChange = { onValueChange(PropValue.BoolValue(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioColors.Primary,
                        checkedTrackColor = StudioColors.PrimaryDim,
                        uncheckedThumbColor = StudioColors.TextTertiary,
                        uncheckedTrackColor = StudioColors.SurfaceHighlight
                    ),
                    modifier = Modifier.scale(0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (value.value) "True" else "False",
                    style = StudioTypography.MonoSmall,
                    color = if (value.value) StudioColors.Primary else StudioColors.TextTertiary,
                    fontSize = 9.sp
                )
            }
        }
        is PropValue.ColorValue -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(value.value)
                        .border(1.dp, StudioColors.PropInputBorder, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "#%06X".format(0xFFFFFF and value.value.toArgb()),
                    style = StudioTypography.MonoSmall,
                    color = StudioColors.PropValueText,
                    fontSize = 9.sp
                )
            }
        }
        is PropValue.EnumValue -> {
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = value.value,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText, fontSize = 9.sp),
                    modifier = Modifier.fillMaxWidth().height(22.dp).clickable { expanded = true },
                    colors = TextFieldColors(),
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, null, tint = StudioColors.TextTertiary, modifier = Modifier.size(14.dp))
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    value.options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    style = StudioTypography.MonoText,
                                    color = if (option == value.value) StudioColors.Primary else StudioColors.TextPrimary,
                                    fontSize = 10.sp
                                )
                            },
                            onClick = {
                                onValueChange(PropValue.EnumValue(option, value.options))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        is PropValue.Vector2Value -> {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                Vector2Field(label = "X", value = value.x, onValueChange = { onValueChange(PropValue.Vector2Value(it, value.y)) })
                Vector2Field(label = "Y", value = value.y, onValueChange = { onValueChange(PropValue.Vector2Value(value.x, it)) })
            }
        }
        is PropValue.UDim2Value -> {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UDimField(label = "X.Sc", value = value.xScale, onValueChange = { onValueChange(value.copy(xScale = it)) })
                    UDimField(label = "X.Os", value = value.xOffset, onValueChange = { onValueChange(value.copy(xOffset = it)) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UDimField(label = "Y.Sc", value = value.yScale, onValueChange = { onValueChange(value.copy(yScale = it)) })
                    UDimField(label = "Y.Os", value = value.yOffset, onValueChange = { onValueChange(value.copy(yOffset = it)) })
                }
            }
        }
    }
}

@Composable
private fun PropertiesContent(
    element: GuiElement,
    onPropertyChange: (String, String, PropValue) -> Unit
) {
    val props = element.properties.values.toList()
    val categories = props.groupBy { it.category.displayName }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        categories.forEach { (category, categoryProps) ->
            PropertyCategory(name = category)
            categoryProps.forEach { prop ->
                PropertyRow(label = prop.displayName) {
                    PropertyValueEditor(
                        prop = prop,
                        onValueChange = { newValue ->
                            onPropertyChange(element.id, prop.key, newValue)
                        }
                    )
                }
            }
        }
    }
}

// --- Compact field editors ---

@Composable
private fun Vector2Field(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    var text by remember(value) { mutableStateOf(String.format("%.1f", value)) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary,
            fontSize = 8.sp,
            modifier = Modifier.width(14.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText, fontSize = 9.sp),
            modifier = Modifier.weight(1f).height(20.dp),
            colors = TextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val parsed = text.toFloatOrNull()
                    if (parsed != null) onValueChange(parsed)
                }
            )
        )
    }
}

@Composable
private fun UDimField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    var text by remember(value) { mutableStateOf(String.format("%.0f", value)) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            style = StudioTypography.MonoSmall,
            color = StudioColors.TextTertiary,
            fontSize = 8.sp,
            modifier = Modifier.width(22.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText, fontSize = 9.sp),
            modifier = Modifier.weight(1f).height(20.dp),
            colors = TextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val parsed = text.toFloatOrNull()
                    if (parsed != null) onValueChange(parsed)
                }
            )
        )
    }
}

@Composable
private fun TextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = StudioColors.Primary,
    unfocusedBorderColor = StudioColors.PropInputBorder,
    focusedContainerColor = StudioColors.BackgroundInput,
    unfocusedContainerColor = StudioColors.BackgroundInput,
    cursorColor = StudioColors.Primary
)
