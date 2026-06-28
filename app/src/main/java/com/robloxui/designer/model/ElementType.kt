package com.robloxui.designer.model

/**
 * Represents all supported Roblox GUI element types.
 */
enum class ElementType(
    val displayName: String,
    val className: String,   // Roblox class name for export
    val icon: String,        // Icon name for lookup
    val category: String,    // Category in toolbox
    val canHaveChildren: Boolean
) {
    // Container types
    FRAME("Frame", "Frame", "frame", "Containers", true),
    SCOPING_FRAME("ScrollingFrame", "ScrollingFrame", "scrolling_frame", "Containers", true),
    CANVAS("CanvasGroup", "CanvasGroup", "canvas_group", "Containers", true),

    // UI Components
    BUTTON("TextButton", "TextButton", "text_button", "Buttons", false),
    IMAGE_BUTTON("ImageButton", "ImageButton", "image_button", "Buttons", false),

    // Labels
    TEXT_LABEL("TextLabel", "TextLabel", "text_label", "Labels", false),
    TEXT_BOX("TextBox", "TextBox", "text_box", "Labels", false),

    // Visual
    IMAGE_LABEL("ImageLabel", "ImageLabel", "image_label", "Visual", false),
    VIEWPORT_FRAME("ViewportFrame", "ViewportFrame", "viewport_frame", "Visual", true),
    VIDEO_FRAME("VideoFrame", "VideoFrame", "video_frame", "Visual", false),

    // UI Layout
    UI_LIST_LAYOUT("UIListLayout", "UIListLayout", "ui_list", "Layout", false),
    UI_GRID_LAYOUT("UIGridLayout", "UIGridLayout", "ui_grid", "Layout", false),
    UI_TABLE_LAYOUT("UITableLayout", "UITableLayout", "ui_table", "Layout", false),
    UI_PADDING("UIPadding", "UIPadding", "ui_padding", "Layout", false),

    // Decoration
    UI_CORNER("UICorner", "UICorner", "ui_corner", "Decoration", false),
    UI_STROKE("UIStroke", "UIStroke", "ui_stroke", "Decoration", false),
    UI_GRADIENT("UIGradient", "UIGradient", "ui_gradient", "Decoration", false);
}
