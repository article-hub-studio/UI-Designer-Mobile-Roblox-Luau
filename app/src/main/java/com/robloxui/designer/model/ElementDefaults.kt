package com.robloxui.designer.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object ElementDefaults {

    fun getDefaultProperties(type: ElementType): Map<String, Property> = when (type) {
        ElementType.FRAME -> frameProps()
        ElementType.SCOPING_FRAME -> scrollingFrameProps()
        ElementType.CANVAS -> canvasProps()
        ElementType.BUTTON -> textButtonProps()
        ElementType.IMAGE_BUTTON -> imageButtonProps()
        ElementType.TEXT_LABEL -> textLabelProps()
        ElementType.TEXT_BOX -> textBoxProps()
        ElementType.IMAGE_LABEL -> imageLabelProps()
        ElementType.VIEWPORT_FRAME -> viewportFrameProps()
        ElementType.VIDEO_FRAME -> videoFrameProps()
        ElementType.UI_LIST_LAYOUT -> listLayoutProps()
        ElementType.UI_GRID_LAYOUT -> gridLayoutProps()
        ElementType.UI_TABLE_LAYOUT -> tableLayoutProps()
        ElementType.UI_PADDING -> paddingProps()
        ElementType.UI_CORNER -> cornerProps()
        ElementType.UI_STROKE -> strokeProps()
        ElementType.UI_GRADIENT -> gradientProps()
    }

    private fun baseLayoutProps() = mapOf(
        "Position" to Property(
            "Position", "Position", "Position of the element",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 0f)
        ),
        "Size" to Property(
            "Size", "Size", "Size of the element",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(1f, 0f, 1f, 0f)
        ),
        "AnchorPoint" to Property(
            "AnchorPoint", "Anchor Point", "Anchor point of the element",
            PropCategory.LAYOUT,
            PropValue.Vector2Value(0.5f, 0.5f)
        ),
        "Rotation" to Property(
            "Rotation", "Rotation", "Rotation in degrees",
            PropCategory.LAYOUT,
            PropValue.FloatValue(0f)
        ),
        "Visible" to Property(
            "Visible", "Visible", "Whether the element is visible",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(true)
        ),
        "ZIndex" to Property(
            "ZIndex", "ZIndex", "Z-index ordering",
            PropCategory.BEHAVIOR,
            PropValue.IntValue(1)
        ),
        "BackgroundColor3" to Property(
            "BackgroundColor3", "Background Color", "Background color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFF2B2B2B))
        ),
        "BackgroundTransparency" to Property(
            "BackgroundTransparency", "Bg Transparency",
            "Background transparency (0=opaque, 1=invisible)",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "BorderColor3" to Property(
            "BorderColor3", "Border Color", "Border color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFF111111))
        ),
        "BorderSizePixel" to Property(
            "BorderSizePixel", "Border Size (px)", "Border thickness in pixels",
            PropCategory.APPEARANCE,
            PropValue.IntValue(0)
        )
    )

    private fun frameProps() = baseLayoutProps() + mapOf(
        "ClipsDescendants" to Property(
            "ClipsDescendants", "Clip Descendants", "Clip children to bounds",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(false)
        )
    )

    private fun scrollingFrameProps() = baseLayoutProps() + mapOf(
        "ClipsDescendants" to Property(
            "ClipsDescendants", "Clip Descendants", "Clip children to bounds",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(true)
        ),
        "ScrollBarThickness" to Property(
            "ScrollBarThickness", "Scrollbar Thickness",
            "Thickness of the scrollbar in pixels",
            PropCategory.APPEARANCE,
            PropValue.IntValue(12)
        ),
        "ScrollingDirection" to Property(
            "ScrollingDirection", "Scroll Direction",
            "Direction of scrolling",
            PropCategory.BEHAVIOR,
            PropValue.EnumValue("Y", listOf("X", "Y", "XY"))
        ),
        "ScrollBarImageColor3" to Property(
            "ScrollBarImageColor3", "Scrollbar Color", "Scrollbar color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFF606060))
        ),
        "CanvasSize" to Property(
            "CanvasSize", "Canvas Size", "Virtual canvas size",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 0f)
        )
    )

    private fun canvasProps() = baseLayoutProps() + mapOf(
        "GroupTransparency" to Property(
            "GroupTransparency", "Group Transparency",
            "Transparency applied to all children",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        )
    )

    private fun textLabelProps() = baseLayoutProps() + textProps() + mapOf(
        "Text" to Property(
            "Text", "Text", "The displayed text",
            PropCategory.TEXT,
            PropValue.StringValue("Label")
        ),
        "TextColor3" to Property(
            "TextColor3", "Text Color", "Text color",
            PropCategory.TEXT,
            PropValue.ColorValue(Color(0xFFFFFFFF))
        ),
        "TextScaled" to Property(
            "TextScaled", "Auto Scale",
            "Automatically scale text to fit",
            PropCategory.TEXT,
            PropValue.BoolValue(false)
        ),
        "TextSize" to Property(
            "TextSize", "Text Size", "Font size in points",
            PropCategory.TEXT,
            PropValue.IntValue(14)
        ),
        "TextWrapped" to Property(
            "TextWrapped", "Text Wrapped", "Wrap text to multiple lines",
            PropCategory.TEXT,
            PropValue.BoolValue(true)
        )
    )

    private fun textButtonProps() = textLabelProps() + mapOf(
        "Text" to Property(
            "Text", "Text", "The displayed text",
            PropCategory.TEXT,
            PropValue.StringValue("Button")
        ),
        "AutoButtonColor" to Property(
            "AutoButtonColor", "Auto Button Color",
            "Automatically change color on hover/click",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(true)
        ),
        "Modal" to Property(
            "Modal", "Modal",
            "Capture all input when clicked",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(false)
        ),
        "Selected" to Property(
            "Selected", "Selected",
            "Whether the button is selected",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(false)
        )
    )

    private fun imageButtonProps() = baseLayoutProps() + mapOf(
        "Image" to Property(
            "Image", "Image", "Image asset ID",
            PropCategory.DATA,
            PropValue.StringValue("rbxassetid://0")
        ),
        "ImageColor3" to Property(
            "ImageColor3", "Image Color", "Image tint color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFFFFFFFF))
        ),
        "ImageTransparency" to Property(
            "ImageTransparency", "Image Transparency",
            "Image transparency (0=opaque, 1=invisible)",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "AutoButtonColor" to Property(
            "AutoButtonColor", "Auto Button Color",
            "Automatically change color on hover/click",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(true)
        ),
        "ScaleType" to Property(
            "ScaleType", "Scale Type",
            "How the image fits in the element",
            PropCategory.APPEARANCE,
            PropValue.EnumValue("Stretch", listOf("Stretch", "Fit", "Crop", "Slice"))
        )
    )

    private fun textBoxProps() = textLabelProps() + mapOf(
        "Text" to Property(
            "Text", "Text", "The displayed text",
            PropCategory.TEXT,
            PropValue.StringValue("")
        ),
        "PlaceholderText" to Property(
            "PlaceholderText", "Placeholder",
            "Placeholder text when empty",
            PropCategory.TEXT,
            PropValue.StringValue("Enter text...")
        ),
        "ClearTextOnFocus" to Property(
            "ClearTextOnFocus", "Clear on Focus",
            "Clear text when focused",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(true)
        ),
        "MultiLine" to Property(
            "MultiLine", "Multi-Line",
            "Allow multiple lines of text",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(false)
        )
    )

    private fun imageLabelProps() = baseLayoutProps() + mapOf(
        "Image" to Property(
            "Image", "Image", "Image asset ID",
            PropCategory.DATA,
            PropValue.StringValue("rbxassetid://0")
        ),
        "ImageColor3" to Property(
            "ImageColor3", "Image Color", "Image tint color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFFFFFFFF))
        ),
        "ImageTransparency" to Property(
            "ImageTransparency", "Image Transparency",
            "Image transparency (0=opaque, 1=invisible)",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "ScaleType" to Property(
            "ScaleType", "Scale Type",
            "How the image fits in the element",
            PropCategory.APPEARANCE,
            PropValue.EnumValue("Stretch", listOf("Stretch", "Fit", "Crop", "Slice"))
        )
    )

    private fun viewportFrameProps() = baseLayoutProps() + mapOf(
        "ImageTransparency" to Property(
            "ImageTransparency", "Image Transparency",
            "Transparency of the viewport",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "Ambient" to Property(
            "Ambient", "Ambient", "Ambient lighting color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFF888888))
        )
    )

    private fun videoFrameProps() = baseLayoutProps() + mapOf(
        "Video" to Property(
            "Video", "Video", "Video asset ID",
            PropCategory.DATA,
            PropValue.StringValue("rbxassetid://0")
        ),
        "Looped" to Property(
            "Looped", "Looped", "Loop the video",
            PropCategory.BEHAVIOR,
            PropValue.BoolValue(true)
        ),
        "Volume" to Property(
            "Volume", "Volume", "Volume (0-1)",
            PropCategory.BEHAVIOR,
            PropValue.FloatValue(1f)
        )
    )

    private fun listLayoutProps() = mapOf(
        "SortOrder" to Property(
            "SortOrder", "Sort Order",
            "Ordering of children in the list",
            PropCategory.LAYOUT,
            PropValue.EnumValue("LayoutOrder", listOf("LayoutOrder", "Name", "Custom"))
        ),
        "Padding" to Property(
            "Padding", "Padding",
            "Spacing between elements (UDim)",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 4f)
        ),
        "FillDirection" to Property(
            "FillDirection", "Direction",
            "Direction of the layout",
            PropCategory.LAYOUT,
            PropValue.EnumValue("Vertical", listOf("Vertical", "Horizontal"))
        ),
        "HorizontalAlignment" to Property(
            "HorizontalAlignment", "H Alignment",
            "Horizontal alignment of items",
            PropCategory.LAYOUT,
            PropValue.EnumValue("Center", listOf("Left", "Center", "Right"))
        ),
        "VerticalAlignment" to Property(
            "VerticalAlignment", "V Alignment",
            "Vertical alignment of items",
            PropCategory.LAYOUT,
            PropValue.EnumValue("Top", listOf("Top", "Center", "Bottom"))
        )
    )

    private fun gridLayoutProps() = mapOf(
        "SortOrder" to Property(
            "SortOrder", "Sort Order",
            "Ordering of children in the grid",
            PropCategory.LAYOUT,
            PropValue.EnumValue("LayoutOrder", listOf("LayoutOrder", "Name", "Custom"))
        ),
        "HorizontalAlignment" to Property(
            "HorizontalAlignment", "H Alignment",
            "Horizontal alignment of items",
            PropCategory.LAYOUT,
            PropValue.EnumValue("Center", listOf("Left", "Center", "Right"))
        ),
        "VerticalAlignment" to Property(
            "VerticalAlignment", "V Alignment",
            "Vertical alignment of items",
            PropCategory.LAYOUT,
            PropValue.EnumValue("Top", listOf("Top", "Center", "Bottom"))
        ),
        "CellSize" to Property(
            "CellSize", "Cell Size",
            "Size of each cell in the grid",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 100f, 0f, 100f)
        ),
        "CellPadding" to Property(
            "CellPadding", "Cell Padding",
            "Padding between cells",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 4f, 0f, 4f)
        ),
        "StartCorner" to Property(
            "StartCorner", "Start Corner",
            "Which corner to start filling from",
            PropCategory.LAYOUT,
            PropValue.EnumValue("TopLeft", listOf("TopLeft", "TopRight", "BottomLeft", "BottomRight"))
        )
    )

    private fun tableLayoutProps() = mapOf(
        "FillEmptySpaceColumns" to Property(
            "FillEmptySpaceColumns", "Fill Empty Columns",
            "Fill empty space in columns",
            PropCategory.LAYOUT,
            PropValue.BoolValue(false)
        ),
        "FillEmptySpaceRows" to Property(
            "FillEmptySpaceRows", "Fill Empty Rows",
            "Fill empty space in rows",
            PropCategory.LAYOUT,
            PropValue.BoolValue(false)
        ),
        "MajorAxis" to Property(
            "MajorAxis", "Major Axis",
            "Primary fill direction",
            PropCategory.LAYOUT,
            PropValue.EnumValue("Row", listOf("Row", "Column"))
        )
    )

    private fun paddingProps() = mapOf(
        "PaddingLeft" to Property(
            "PaddingLeft", "Padding Left",
            "Left padding in pixels",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 0f)
        ),
        "PaddingRight" to Property(
            "PaddingRight", "Padding Right",
            "Right padding in pixels",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 0f)
        ),
        "PaddingTop" to Property(
            "PaddingTop", "Padding Top",
            "Top padding in pixels",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 0f)
        ),
        "PaddingBottom" to Property(
            "PaddingBottom", "Padding Bottom",
            "Bottom padding in pixels",
            PropCategory.LAYOUT,
            PropValue.UDim2Value(0f, 0f, 0f, 0f)
        )
    )

    private fun cornerProps() = mapOf(
        "CornerRadius" to Property(
            "CornerRadius", "Corner Radius",
            "Radius of the corner (UDim)",
            PropCategory.APPEARANCE,
            PropValue.UDim2Value(0f, 8f, 0f, 8f)
        )
    )

    private fun strokeProps() = mapOf(
        "Thickness" to Property(
            "Thickness", "Thickness",
            "Stroke thickness in pixels",
            PropCategory.APPEARANCE,
            PropValue.IntValue(1)
        ),
        "Color" to Property(
            "Color", "Color",
            "Stroke color",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFFFFFFFF))
        ),
        "Transparency" to Property(
            "Transparency", "Transparency",
            "Stroke transparency (0=opaque)",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "ApplyStrokeMode" to Property(
            "ApplyStrokeMode", "Apply Mode",
            "How the stroke is applied",
            PropCategory.APPEARANCE,
            PropValue.EnumValue("Border", listOf("Border", "Outline", "Inset"))
        )
    )

    private fun gradientProps() = mapOf(
        "Color" to Property(
            "Color", "Color",
            "Gradient color sequence (CSeries)",
            PropCategory.APPEARANCE,
            PropValue.ColorValue(Color(0xFFFFFFFF))
        ),
        "Rotation" to Property(
            "Rotation", "Rotation",
            "Rotation of the gradient in degrees",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "Transparency" to Property(
            "Transparency", "Transparency",
            "Gradient transparency (NSeries)",
            PropCategory.APPEARANCE,
            PropValue.FloatValue(0f)
        ),
        "Offset" to Property(
            "Offset", "Offset",
            "Offset of the gradient",
            PropCategory.APPEARANCE,
            PropValue.Vector2Value(0f, 0f)
        )
    )

    private fun textProps() = mapOf(
        "Font" to Property(
            "Font", "Font",
            "Font face",
            PropCategory.TEXT,
            PropValue.EnumValue("Gotham", listOf(
                "Gotham", "GothamMedium", "GothamBold", "GothamBlack",
                "SourceSans", "SourceSansLight", "SourceSansBold",
                "Arial", "ArialBold", "Roboto", "RobotoMono"
            ))
        ),
        "TextXAlignment" to Property(
            "TextXAlignment", "H Align",
            "Horizontal alignment",
            PropCategory.TEXT,
            PropValue.EnumValue("Center", listOf("Left", "Center", "Right"))
        ),
        "TextYAlignment" to Property(
            "TextYAlignment", "V Align",
            "Vertical alignment",
            PropCategory.TEXT,
            PropValue.EnumValue("Center", listOf("Top", "Center", "Bottom"))
        ),
        "TextStrokeColor3" to Property(
            "TextStrokeColor3", "Stroke Color",
            "Text stroke/outline color",
            PropCategory.TEXT,
            PropValue.ColorValue(Color(0xFF000000))
        ),
        "TextStrokeTransparency" to Property(
            "TextStrokeTransparency", "Stroke Transparency",
            "Text stroke transparency (0=opaque, 1=invisible)",
            PropCategory.TEXT,
            PropValue.FloatValue(1f)
        )
    )
}
