package com.robloxui.designer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.robloxui.designer.model.*
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.ui.theme.StudioTypography

@Composable
fun PropertiesPanel(
    element: GuiElement?,
    onPropertyChange: (String, String, PropValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(StudioColors.PropBg)) {
        // Header
        PropertiesHeader(element)

        Divider(color = StudioColors.ToolbarDivider, thickness = 1.dp)

        if (element == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = StudioColors.TextTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Select an element\nto edit its properties",
                        style = StudioTypography.MonoText,
                        color = StudioColors.TextDisabled
                    )
                }
            }
        } else {
            val groupedProperties = element.properties.values.groupBy { it.category }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                groupedProperties.forEach { (category, props) ->
                    item {
                        PropertyCategory(category.displayName)
                    }
                    items(props) { property ->
                        PropertyRow(
                            property = property,
                            onValueChange = { newValue ->
                                onPropertyChange(element.id, property.name, newValue)
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PropertiesHeader(element: GuiElement?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioColors.BackgroundDarker)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = "Properties",
            tint = StudioColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Properties",
            style = StudioTypography.MonoLabel,
            color = StudioColors.TextSecondary
        )
        if (element != null) {
            Spacer(modifier = Modifier.weight(1f))
            ElementTypeIcon(type = element.type, size = 14)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                element.name,
                style = StudioTypography.MonoSmall,
                color = StudioColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PropertyCategory(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioColors.PropCategoryBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            style = StudioTypography.MonoLabel,
            color = StudioColors.Primary,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}

@Composable
private fun PropertyRow(
    property: Property,
    onValueChange: (PropValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioColors.PropRowBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Property name
        Text(
            text = property.displayName,
            style = StudioTypography.MonoSmall,
            color = StudioColors.PropLabelText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(100.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Property value editor
        Box(modifier = Modifier.weight(1f)) {
            PropertyValueEditor(
                value = property.value,
                onValueChange = onValueChange
            )
        }
    }
}

@Composable
private fun PropertyValueEditor(
    value: PropValue,
    onValueChange: (PropValue) -> Unit
) {
    when (value) {
        is PropValue.StringValue -> {
            var text by remember(value) { mutableStateOf(value.value) }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onValueChange(PropValue.StringValue(it))
                },
                singleLine = true,
                textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = TextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        is PropValue.FloatValue -> {
            var text by remember(value) { mutableStateOf(String.format("%.2f", value.value)) }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = TextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
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
                textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = TextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
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
                    modifier = Modifier.scale(0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (value.value) "True" else "False",
                    style = StudioTypography.MonoSmall,
                    color = if (value.value) StudioColors.Primary else StudioColors.TextTertiary
                )
            }
        }
        is PropValue.ColorValue -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color swatch
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(value.value)
                        .border(1.dp, StudioColors.PropInputBorder, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "#%06X".format(0xFFFFFF and value.value.toArgb()),
                    style = StudioTypography.MonoSmall,
                    color = StudioColors.PropValueText
                )
            }
        }
        is PropValue.EnumValue -> {
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StudioColors.PropValueText
                    ),
                    border = BorderStroke(1.dp, StudioColors.PropInputBorder),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        value.value,
                        style = StudioTypography.MonoSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(StudioColors.Surface)
                ) {
                    value.options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    style = StudioTypography.MonoText,
                                    color = if (option == value.value) StudioColors.Primary
                                    else StudioColors.TextPrimary
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Vector2Field(
                    label = "X",
                    value = value.x,
                    onValueChange = { onValueChange(PropValue.Vector2Value(it, value.y)) }
                )
                Vector2Field(
                    label = "Y",
                    value = value.y,
                    onValueChange = { onValueChange(PropValue.Vector2Value(value.x, it)) }
                )
            }
        }
        is PropValue.UDim2Value -> {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UDimField(
                        label = "X.Sc",
                        value = value.xScale,
                        onValueChange = { onValueChange(value.copy(xScale = it)) }
                    )
                    UDimField(
                        label = "X.Os",
                        value = value.xOffset,
                        onValueChange = { onValueChange(value.copy(xOffset = it)) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UDimField(
                        label = "Y.Sc",
                        value = value.yScale,
                        onValueChange = { onValueChange(value.copy(yScale = it)) }
                    )
                    UDimField(
                        label = "Y.Os",
                        value = value.yOffset,
                        onValueChange = { onValueChange(value.copy(yOffset = it)) }
                    )
                }
            }
        }
    }
}

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
            modifier = Modifier.width(18.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText),
            modifier = Modifier
                .weight(1f)
                .height(28.dp),
            colors = TextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
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
            modifier = Modifier.width(28.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = StudioTypography.MonoSmall.copy(color = StudioColors.PropValueText),
            modifier = Modifier
                .weight(1f)
                .height(28.dp),
            colors = TextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
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
