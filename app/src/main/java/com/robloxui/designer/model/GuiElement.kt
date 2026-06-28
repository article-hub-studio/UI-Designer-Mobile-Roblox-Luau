package com.robloxui.designer.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Represents a single GUI element (instance) in the design tree.
 */
data class GuiElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType,
    val name: String = type.displayName,
    val children: List<GuiElement> = emptyList(),
    val properties: Map<String, Property> = ElementDefaults.getDefaultProperties(type),
    val expanded: Boolean = true,
    val visible: Boolean = true,
    val locked: Boolean = false,
    // Visual-only state (not exported)
    val zIndex: Int = 0
) {
    /**
     * Convenience accessor for common properties.
     */
    fun prop(name: String): Property? = properties[name]

    fun withProperty(name: String, newValue: PropValue): GuiElement {
        val old = properties[name] ?: return this
        return copy(properties = properties + (name to old.copy(value = newValue)))
    }

    fun withChildren(newChildren: List<GuiElement>): GuiElement {
        return copy(children = newChildren)
    }

    fun addChild(child: GuiElement): GuiElement {
        return copy(children = children + child)
    }

    fun removeChild(childId: String): GuiElement {
        return copy(children = children.filter { it.id != childId })
    }

    fun replaceChild(childId: String, newChild: GuiElement): GuiElement {
        return copy(children = children.map { if (it.id == childId) newChild else it })
    }

    fun findById(targetId: String): GuiElement? {
        if (id == targetId) return this
        for (child in children) {
            val found = child.findById(targetId)
            if (found != null) return found
        }
        return null
    }

    fun findParentOf(targetId: String): GuiElement? {
        for (child in children) {
            if (child.id == targetId) return this
            val found = child.findParentOf(targetId)
            if (found != null) return found
        }
        return null
    }

    /**
     * Build a flat list of all descendants (pre-order traversal).
     */
    fun flatten(depth: Int = 0): List<Pair<GuiElement, Int>> {
        val list = mutableListOf(this to depth)
        for (child in children) {
            list.addAll(child.flatten(depth + 1))
        }
        return list
    }

    /**
     * Deep copy with new IDs.
     */
    fun deepCopy(newId: Boolean = true): GuiElement {
        return copy(
            id = if (newId) UUID.randomUUID().toString() else id,
            children = children.map { it.deepCopy(newId) }
        )
    }
}
