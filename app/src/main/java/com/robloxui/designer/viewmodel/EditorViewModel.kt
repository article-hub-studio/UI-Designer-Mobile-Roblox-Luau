package com.robloxui.designer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.robloxui.designer.model.ElementType
import com.robloxui.designer.model.GuiElement
import com.robloxui.designer.model.PropValue
import com.robloxui.designer.model.Property
import java.util.UUID

data class EditorState(
    val rootElement: GuiElement = GuiElement(
        type = ElementType.FRAME,
        name = "ScreenGui",
        expanded = true,
        properties = emptyMap()
    ),
    val selectedElementId: String? = null,
    val clipboard: GuiElement? = null,
    val undoStack: List<GuiElement> = emptyList(),
    val redoStack: List<GuiElement> = emptyList(),
    val activePanel: EditorPanel = EditorPanel.EXPLORER,
    val previewMode: Boolean = false,
    val showExportDialog: Boolean = false,
    val tool: EditorTool = EditorTool.POINTER,
    val zoom: Float = 1f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f
)

enum class EditorPanel { EXPLORER, PROPERTIES, TOOLBOX, CANVAS }
enum class EditorTool { POINTER, MOVE, ADD_FRAME, ADD_BUTTON, ADD_LABEL, ADD_IMAGE }

const val MAX_UNDO = 50

class EditorViewModel : ViewModel() {

    var state by mutableStateOf(EditorState())
        private set

    // ── Selection ──

    val selectedElement: GuiElement?
        get() = state.selectedElementId?.let { id ->
            state.rootElement.findById(id)
        }

    val selectedElementParent: GuiElement?
        get() = state.selectedElementId?.let { id ->
            state.rootElement.findParentOf(id)
        }

    /** All elements flattened for the explorer tree */
    val flatElements: List<Pair<GuiElement, Int>>
        get() = state.rootElement.flatten()

    fun selectElement(id: String?) {
        state = state.copy(selectedElementId = id)
    }

    // ── Tree mutations ──

    fun addElement(type: ElementType, parentId: String? = null) {
        val newElement = GuiElement(type = type)
        val targetParentId = parentId ?: state.selectedElementId ?: state.rootElement.id

        if (targetParentId == state.rootElement.id) {
            pushUndo()
            state = state.copy(
                rootElement = state.rootElement.addChild(newElement),
                selectedElementId = newElement.id
            )
        } else {
            val parent = state.rootElement.findById(targetParentId)
            if (parent != null && parent.type.canHaveChildren) {
                pushUndo()
                val updated = updateElementInTree(targetParentId) { p ->
                    p.addChild(newElement)
                }
                state = state.copy(
                    rootElement = updated,
                    selectedElementId = newElement.id
                )
            }
        }
    }

    fun deleteElement(id: String) {
        if (id == state.rootElement.id) return
        pushUndo()
        val newRoot = removeFromTree(state.rootElement, id)
        state = state.copy(
            rootElement = newRoot,
            selectedElementId = state.rootElement.id
        )
    }

    fun duplicateElement(id: String) {
        val element = state.rootElement.findById(id) ?: return
        val clone = element.deepCopy(newId = true)
            .copy(name = "${element.name} Copy")

        val parent = state.rootElement.findParentOf(id)
        val parentId = parent?.id ?: state.rootElement.id

        pushUndo()
        if (parentId == state.rootElement.id) {
            state = state.copy(
                rootElement = state.rootElement.addChild(clone),
                selectedElementId = clone.id
            )
        } else {
            val updated = updateElementInTree(parentId) { p ->
                p.addChild(clone)
            }
            state = state.copy(
                rootElement = updated,
                selectedElementId = clone.id
            )
        }
    }

    fun moveElementByDelta(elementId: String, dx: Float, dy: Float) {
        val element = state.rootElement.findById(elementId) ?: return
        val pos = element.prop("Position")?.value as? PropValue.UDim2Value ?: return
        val newPos = pos.copy(xOffset = pos.xOffset + dx, yOffset = pos.yOffset + dy)
        // Push undo on first drag delta only (dx != 0 or dy != 0)
        if (dx != 0f || dy != 0f) {
            updateProperty(elementId, "Position", newPos)
        }
    }

    fun updateProperty(elementId: String, propName: String, newValue: PropValue) {
        pushUndo()
        val updated = updateElementInTree(elementId) { el ->
            el.withProperty(propName, newValue)
        }
        state = state.copy(rootElement = updated)
    }

    // ── Clipboard ──

    fun copyElement(id: String) {
        val element = state.rootElement.findById(id)
        state = state.copy(clipboard = element?.deepCopy(newId = false))
    }

    fun pasteElement() {
        val clip = state.clipboard ?: return
        val clone = clip.deepCopy(newId = true)
            .copy(name = "${clip.name} (Paste)")

        val targetId = state.selectedElementId ?: state.rootElement.id
        pushUndo()

        if (targetId == state.rootElement.id) {
            state = state.copy(
                rootElement = state.rootElement.addChild(clone),
                selectedElementId = clone.id
            )
        } else {
            val target = state.rootElement.findById(targetId)
            if (target != null && target.type.canHaveChildren) {
                val updated = updateElementInTree(targetId) { p ->
                    p.addChild(clone)
                }
                state = state.copy(
                    rootElement = updated,
                    selectedElementId = clone.id
                )
            }
        }
    }

    // ── Undo / Redo ──

    private fun pushUndo() {
        val stack = (listOf(state.rootElement) + state.undoStack).take(MAX_UNDO)
        state = state.copy(
            undoStack = stack,
            redoStack = emptyList()
        )
    }

    fun undo() {
        if (state.undoStack.isEmpty()) return
        val prev = state.undoStack.first()
        val newRedo = listOf(state.rootElement) + state.redoStack
        state = state.copy(
            rootElement = prev,
            undoStack = state.undoStack.drop(1),
            redoStack = newRedo
        )
    }

    fun redo() {
        if (state.redoStack.isEmpty()) return
        val next = state.redoStack.first()
        val newUndo = listOf(state.rootElement) + state.undoStack
        state = state.copy(
            rootElement = next,
            undoStack = newUndo,
            redoStack = state.redoStack.drop(1)
        )
    }

    // ── Panel / tool state ──

    fun setActivePanel(panel: EditorPanel) {
        state = state.copy(activePanel = panel)
    }

    fun togglePreview() {
        state = state.copy(previewMode = !state.previewMode)
    }

    fun setShowExportDialog(show: Boolean) {
        state = state.copy(showExportDialog = show)
    }

    fun setTool(tool: EditorTool) {
        state = state.copy(tool = tool)
    }

    fun setZoom(zoom: Float) {
        state = state.copy(zoom = zoom.coerceIn(0.1f, 5f))
    }

    fun setPan(x: Float, y: Float) {
        state = state.copy(panOffsetX = x, panOffsetY = y)
    }

    fun newProject() {
        pushUndo()
        state = state.copy(
            rootElement = GuiElement(
                type = ElementType.FRAME,
                name = "ScreenGui",
                expanded = true,
                properties = emptyMap()
            ),
            selectedElementId = null,
            clipboard = null,
            undoStack = emptyList(),
            redoStack = emptyList(),
            zoom = 1f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            tool = EditorTool.POINTER
        )
    }

    // ── Tree helpers ──

    private fun updateElementInTree(id: String, transform: (GuiElement) -> GuiElement): GuiElement {
        return updateElementInTree(state.rootElement, id, transform)
    }

    private fun updateElementInTree(
        root: GuiElement,
        id: String,
        transform: (GuiElement) -> GuiElement
    ): GuiElement {
        if (root.id == id) return transform(root)
        return root.copy(
            children = root.children.map { child ->
                updateElementInTree(child, id, transform)
            }
        )
    }

    private fun removeFromTree(root: GuiElement, targetId: String): GuiElement {
        if (root.id == targetId) return root // can't remove root
        return root.copy(
            children = root.children.filter { it.id != targetId }.map { child ->
                removeFromTree(child, targetId)
            }
        )
    }
}
