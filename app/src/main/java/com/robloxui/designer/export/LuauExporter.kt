package com.robloxui.designer.export

import com.robloxui.designer.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Standalone Luau code generator that converts a GuiElement tree
 * into executable Roblox Luau code.
 *
 * Supports: Delta Executor, Roblox Studio, and other executors.
 */
class LuauExporter {

    data class ExportResult(
        val code: String,
        val elementCount: Int,
        val lineCount: Int,
        val errors: List<String> = emptyList()
    )

    /**
     * Full export with auto-generated Instance IDs and parent-child relationships.
     */
    fun export(
        rootElement: GuiElement,
        options: ExportOptions = ExportOptions()
    ): ExportResult {
        val sb = StringBuilder()
        val errors = mutableListOf<String>()
        val elementCount = countElements(rootElement)
        val varMap = mutableMapOf<String, String>() // id -> variable name

        // Header
        sb.appendLine("--[[")
        sb.appendLine("  Roblox UI Designer - Exported Layout")
        sb.appendLine("  Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        sb.appendLine("  Elements: $elementCount")
        sb.appendLine("  Format: Luau")
        sb.appendLine("  Executor: ${options.executorName}")
        sb.appendLine("--]]")
        sb.appendLine()

        if (options.includeServices) {
            sb.appendLine("-- Services")
            sb.appendLine("local Players = game:GetService(\"Players\")")
            if (options.useTweenService) {
                sb.appendLine("local TweenService = game:GetService(\"TweenService\")")
            }
            if (options.useUserInputService) {
                sb.appendLine("local UserInputService = game:GetService(\"UserInputService\")")
            }
            sb.appendLine()
        }

        if (options.createScreenGui) {
            sb.appendLine("-- Create ScreenGui")
            sb.appendLine("local ScreenGui = Instance.new(\"ScreenGui\")")
            sb.appendLine("ScreenGui.Name = \"${options.screenGuiName}\"")
            sb.appendLine("ScreenGui.ResetOnSpawn = ${options.resetOnSpawn}")
            if (options.zIndexBehavior == "Sibling") {
                sb.appendLine("ScreenGui.ZIndexBehavior = Enum.ZIndexBehavior.Sibling")
            }
            sb.appendLine()
        }

        sb.appendLine("-- Build UI Hierarchy")
        sb.appendLine()

        // Generate variable names for all elements
        generateVarNames(rootElement, options.screenGuiName, varMap)

        // Generate instances
        generateInstanceCode(rootElement, options, varMap, sb, errors)

        // Parent everything
        sb.appendLine("-- Assigning parents...")
        parentChildCode(rootElement, "ScreenGui", varMap, sb)

        if (options.includeAutoLoad) {
            sb.appendLine()
            sb.appendLine("-- Auto-load to PlayerGui")
            sb.appendLine("if Players.LocalPlayer then")
            sb.appendLine("    ScreenGui.Parent = Players.LocalPlayer:WaitForChild(\"PlayerGui\")")
            sb.appendLine("else")
            sb.appendLine("    Players.LocalPlayerAdded:Connect(function(player)")
            sb.appendLine("        ScreenGui.Parent = player:WaitForChild(\"PlayerGui\")")
            sb.appendLine("    end)")
            sb.appendLine("end")
        }

        if (options.includeReturn) {
            sb.appendLine()
            sb.appendLine("return ScreenGui")
        }

        val lines = sb.toString().count { it == '\n' } + 1
        return ExportResult(sb.toString(), elementCount, lines, errors)
    }

    /**
     * Generate a minimal export targeting executors (Delta, etc.)
     */
    fun exportExecutor(
        rootElement: GuiElement,
        executorType: String = "Delta"
    ): ExportResult {
        return export(rootElement, ExportOptions(
            executorName = executorType,
            includeServices = true,
            createScreenGui = true,
            includeAutoLoad = true,
            resetOnSpawn = false,
            includeReturn = false
        ))
    }

    /**
     * Generate a compact export (all inline, minimal whitespace).
     */
    fun exportCompact(rootElement: GuiElement): String {
        val sb = StringBuilder()
        sb.appendLine("-- Roblox UI Designer Compact Export")
        var counter = 0
        val nameMap = mutableMapOf<String, String>()

        fun generate(el: GuiElement, parentVar: String) {
            counter++
            val varName = "i$counter"
            nameMap[el.id] = varName
            sb.append("local $varName=Instance.new(\"${el.type.className}\") ")
            sb.append("$varName.Name=\"${el.name}\" ")

            // Essential props in one line
            val pos = el.prop("Position")?.value as? PropValue.UDim2Value
            if (pos != null) sb.append("$varName.Position=UDim2.new(${pos.xScale},${pos.xOffset},${pos.yScale},${pos.yOffset}) ")

            val size = el.prop("Size")?.value as? PropValue.UDim2Value
            if (size != null) sb.append("$varName.Size=UDim2.new(${size.xScale},${size.xOffset},${size.yScale},${size.yOffset}) ")

            sb.append("$varName.Parent=$parentVar")
            sb.append("\n")

            el.children.forEach { generate(it, varName) }
        }

        sb.appendLine("local s=Instance.new(\"ScreenGui\") s.Name=\"${rootElement.name}\" s.ResetOnSpawn=false")
        rootElement.children.forEach { generate(it, "s") }
        sb.appendLine("s.Parent=game:GetService(\"Players\").LocalPlayer:WaitForChild(\"PlayerGui\")")
        return sb.toString()
    }

    /**
     * Export a diff / changelog between two states.
     */
    fun exportDiff(oldRoot: GuiElement, newRoot: GuiElement): String {
        val sb = StringBuilder()
        sb.appendLine("-- UI Diff Report")
        sb.appendLine("-- Old: ${countElements(oldRoot)} elements")
        sb.appendLine("-- New: ${countElements(newRoot)} elements")
        sb.appendLine()
        sb.appendLine("-- Manual updates required:")
        sb.appendLine("-- (Full re-export recommended for complex changes)")
        sb.appendLine()
        sb.appendLine("-- Re-export both versions to compare.")
        return sb.toString()
    }

    // ── Private helpers ──

    private fun generateVarNames(
        element: GuiElement,
        parentName: String,
        varMap: MutableMap<String, String>
    ) {
        element.children.forEachIndexed { i, child ->
            val safeName = child.name
                .replace(" ", "_")
                .replace("-", "_")
                .replace("'", "")
                .replace("\"", "")
                .replace(".", "_")
                .replace("(", "_")
                .replace(")", "_")
            val varName = if (safeName.isNotEmpty() && safeName[0].isLetter()) {
                safeName.substring(0, 1).lowercase() + safeName.drop(1)
            } else {
                "elem_$i"
            }
            varMap[child.id] = varName
            generateVarNames(child, varName, varMap)
        }
    }

    private fun generateInstanceCode(
        element: GuiElement,
        options: ExportOptions,
        varMap: Map<String, String>,
        sb: StringBuilder,
        errors: MutableList<String>
    ) {
        for ((i, child) in element.children.withIndex()) {
            writeInstance(child, varMap, sb, errors)
            generateInstanceCode(child, options, varMap, sb, errors)
        }
    }

    private fun writeInstance(
        element: GuiElement,
        varMap: Map<String, String>,
        sb: StringBuilder,
        errors: MutableList<String>
    ) {
        val v = varMap[element.id] ?: return

        sb.appendLine("-- ${element.type.displayName}: ${element.name}")
        sb.appendLine("local $v = Instance.new(\"${element.type.className}\")")

        // Basic properties
        writeProp(v, element, "Name", PropValue.StringValue(element.name), sb)
        writeProp(v, element, "Visible", PropValue.BoolValue(true), sb) // default
        writeUDim(v, element, "Position", sb)
        writeUDim(v, element, "Size", sb)
        writeVector2(v, element, "AnchorPoint", sb)

        // Appearance
        writeColor3(v, element, "BackgroundColor3", sb)
        writeProp(v, element, "BackgroundTransparency", PropValue.FloatValue(0f), sb)
        writeColor3(v, element, "BorderColor3", sb)
        writeProp(v, element, "BorderSizePixel", PropValue.IntValue(0), sb)
        writeProp(v, element, "Rotation", PropValue.FloatValue(0f), sb)
        writeProp(v, element, "ClipsDescendants", PropValue.BoolValue(false), sb)
        writeProp(v, element, "ZIndex", PropValue.IntValue(1), sb)

        // Text properties
        writeProp(v, element, "Text", null, sb) { it }
        writeColor3(v, element, "TextColor3", sb)
        writeProp(v, element, "TextSize", PropValue.IntValue(14), sb)
        writeFont(v, element, sb)
        writeEnum(v, element, "TextXAlignment", "TextXAlignment", sb)
        writeEnum(v, element, "TextYAlignment", "TextYAlignment", sb)
        writeProp(v, element, "TextScaled", PropValue.BoolValue(false), sb)
        writeProp(v, element, "TextWrapped", PropValue.BoolValue(true), sb)
        writeColor3(v, element, "TextStrokeColor3", sb)
        writeProp(v, element, "TextStrokeTransparency", PropValue.FloatValue(1f), sb)

        // Image properties
        writeImage(v, element, sb)
        writeProp(v, element, "ImageTransparency", PropValue.FloatValue(0f), sb)

        // ScrollingFrame
        writeProp(v, element, "ScrollBarThickness", PropValue.IntValue(12), sb)
        writeEnum(v, element, "ScrollingDirection", "ScrollingDirection", sb)

        // Layout properties
        writeEnum(v, element, "SortOrder", "SortOrder", sb)
        writeEnum(v, element, "FillDirection", "FillDirection", sb)
        writeEnum(v, element, "HorizontalAlignment", "HorizontalAlignment", sb)
        writeEnum(v, element, "VerticalAlignment", "VerticalAlignment", sb)
        writeUDim(v, element, "CellSize", sb)
        writeUDim(v, element, "Padding", sb)

        // UICorner
        val cornerRadius = element.prop("CornerRadius")?.value as? PropValue.UDim2Value
        if (cornerRadius != null) {
            sb.appendLine("$v.CornerRadius = UDim.new(${cornerRadius.xScale}, ${cornerRadius.xOffset})")
        }

        // UIStroke
        writeProp(v, element, "Thickness", PropValue.IntValue(1), sb)
        writeProp(v, element, "Transparency", PropValue.FloatValue(0f), sb)

        sb.appendLine()
    }

    private fun parentChildCode(
        element: GuiElement,
        parentVar: String,
        varMap: Map<String, String>,
        sb: StringBuilder
    ) {
        for (child in element.children) {
            val v = varMap[child.id] ?: continue
            sb.appendLine("$v.Parent = $parentVar")
            parentChildCode(child, v, varMap, sb)
        }
    }

    private fun writeProp(
        varName: String,
        element: GuiElement,
        propName: String,
        defaultValue: PropValue?,
        sb: StringBuilder,
        formatter: ((String) -> String)? = null
    ) {
        val prop = element.prop(propName)?.value
        if (prop == null) {
            // Write default if available
            if (defaultValue != null && formatter != null) {
                sb.appendLine("$varName.$propName = ${formatter(formatPropValue(defaultValue))}")
            }
            return
        }
        when (prop) {
            is PropValue.StringValue -> {
                if (prop.value.isNotEmpty() || propName == "Text") {
                    sb.appendLine("$varName.$propName = \"${escape(prop.value)}\"")
                }
            }
            is PropValue.BoolValue -> {
                sb.appendLine("$varName.$propName = ${prop.value}")
            }
            is PropValue.IntValue -> {
                sb.appendLine("$varName.$propName = ${prop.value}")
            }
            is PropValue.FloatValue -> {
                sb.appendLine("$varName.$propName = ${prop.value}")
            }
            else -> {}
        }
    }

    private fun writeUDim(varName: String, element: GuiElement, propName: String, sb: StringBuilder) {
        val prop = element.prop(propName)?.value as? PropValue.UDim2Value ?: return
        sb.appendLine("$varName.$propName = UDim2.new(${prop.xScale}, ${prop.xOffset}, ${prop.yScale}, ${prop.yOffset})")
    }

    private fun writeVector2(varName: String, element: GuiElement, propName: String, sb: StringBuilder) {
        val prop = element.prop(propName)?.value as? PropValue.Vector2Value ?: return
        sb.appendLine("$varName.$propName = Vector2.new(${prop.x}, ${prop.y})")
    }

    private fun writeColor3(varName: String, element: GuiElement, propName: String, sb: StringBuilder) {
        val prop = element.prop(propName)?.value as? PropValue.ColorValue ?: return
        val r = (prop.value.red * 255).toInt()
        val g = (prop.value.green * 255).toInt()
        val b = (prop.value.blue * 255).toInt()
        if (r != 0 || g != 0 || b != 0) {
            sb.appendLine("$varName.$propName = Color3.fromRGB($r, $g, $b)")
        }
    }

    private fun writeFont(varName: String, element: GuiElement, sb: StringBuilder) {
        val prop = element.prop("Font")?.value as? PropValue.EnumValue ?: return
        sb.appendLine("$varName.Font = Enum.Font.${prop.value}")
    }

    private fun writeEnum(varName: String, element: GuiElement, propName: String, enumName: String, sb: StringBuilder) {
        val prop = element.prop(propName)?.value as? PropValue.EnumValue ?: return
        sb.appendLine("$varName.$propName = Enum.$enumName.${prop.value}")
    }

    private fun writeImage(varName: String, element: GuiElement, sb: StringBuilder) {
        val prop = element.prop("Image")?.value as? PropValue.StringValue ?: return
        if (prop.value.isNotEmpty() && prop.value != "rbxassetid://0") {
            sb.appendLine("$varName.Image = \"${escape(prop.value)}\"")
        }
    }

    private fun formatPropValue(value: PropValue): String = when (value) {
        is PropValue.StringValue -> "\"${escape(value.value)}\""
        is PropValue.BoolValue -> value.value.toString()
        is PropValue.IntValue -> value.value.toString()
        is PropValue.FloatValue -> value.value.toString()
        is PropValue.ColorValue -> throw UnsupportedOperationException("Use writeColor3 instead")
        is PropValue.EnumValue -> "Enum.${value.value}"
        is PropValue.Vector2Value -> "Vector2.new(${value.x}, ${value.y})"
        is PropValue.UDim2Value -> "UDim2.new(${value.xScale}, ${value.xOffset}, ${value.yScale}, ${value.yOffset})"
    }

    private fun escape(s: String): String {
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun countElements(element: GuiElement): Int {
        return 1 + element.children.sumOf { countElements(it) }
    }

    data class ExportOptions(
        val executorName: String = "Roblox Studio",
        val includeServices: Boolean = true,
        val createScreenGui: Boolean = true,
        val screenGuiName: String = "ScreenGui",
        val resetOnSpawn: Boolean = false,
        val zIndexBehavior: String = "Global",
        val useTweenService: Boolean = false,
        val useUserInputService: Boolean = false,
        val includeAutoLoad: Boolean = true,
        val includeReturn: Boolean = false
    )
}
