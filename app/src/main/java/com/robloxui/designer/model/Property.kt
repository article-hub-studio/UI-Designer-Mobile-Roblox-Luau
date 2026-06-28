package com.robloxui.designer.model

import androidx.compose.ui.graphics.Color

/**
 * Property value types for the properties panel.
 */
sealed class PropValue {
    data class StringValue(val value: String) : PropValue()
    data class IntValue(val value: Int) : PropValue()
    data class FloatValue(val value: Float) : PropValue()
    data class BoolValue(val value: Boolean) : PropValue()
    data class ColorValue(val value: Color) : PropValue()
    data class EnumValue(val value: String, val options: List<String>) : PropValue()
    data class Vector2Value(val x: Float, val y: Float) : PropValue()
    data class UDim2Value(
        val xScale: Float, val xOffset: Float,
        val yScale: Float, val yOffset: Float
    ) : PropValue()
}

/**
 * A single property definition for a GUI element.
 */
data class Property(
    val name: String,
    val displayName: String,
    val description: String = "",
    val category: PropCategory = PropCategory.OTHER,
    val value: PropValue,
    val isReadOnly: Boolean = false,
    val defaultValue: PropValue? = null
)

enum class PropCategory(val displayName: String) {
    APPEARANCE("Appearance"),
    LAYOUT("Layout & Position"),
    TEXT("Text & Font"),
    BEHAVIOR("Behavior"),
    DATA("Data"),
    OTHER("Other")
}
