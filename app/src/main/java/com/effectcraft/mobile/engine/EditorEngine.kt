package com.effectcraft.mobile.engine

/** All document mutations pass through this command processor and are undoable. */
class EditorEngine(initial: ProjectDocument = ProjectDocument()) {
    private val undoStack = ArrayDeque<ProjectDocument>()
    private val redoStack = ArrayDeque<ProjectDocument>()
    var document: ProjectDocument = initial
        private set
    var activeComposition: ItemId? = initial.compositions.firstOrNull()?.id
        private set
    var currentTime: Tick = Tick.ZERO
        private set
    var selectedLayer: LayerId? = null
        private set
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val activeComp: Composition? get() = document.compositions.firstOrNull { it.id == activeComposition }

    private fun commit(next: ProjectDocument) {
        if (next == document) return
        undoStack.addLast(document)
        if (undoStack.size > 100) undoStack.removeFirst()
        redoStack.clear()
        document = next
    }
    fun setComposition(id: ItemId) {
        require(document.compositions.any { it.id == id })
        activeComposition = id
        currentTime = Tick.ZERO
        selectedLayer = null
    }
    fun setTime(time: Tick) {
        val comp = activeComp ?: return
        currentTime = Tick(time.value.coerceIn(0L, comp.duration.value))
    }
    fun selectLayer(id: LayerId?) {
        require(id == null || activeComp?.layers?.any { it.id == id } == true)
        selectedLayer = id
    }
    fun addLayer(name: String, kind: LayerKind, duration: Tick = activeComp?.duration ?: Tick.fromSeconds(10.0)): LayerId {
        val comp = activeComp ?: error("No active composition")
        val allocated = document.allocateId()
        val id = LayerId(allocated.first)
        val layer = TimelineLayer(id, name, kind, outPoint = duration)
        val changed = comp.copy(layers = listOf(layer) + comp.layers)
        commit(document.copy(compositions = document.compositions.map { if (it.id == comp.id) changed else it }, nextId = allocated.second.nextId))
        selectedLayer = id
        return id
    }
    fun updateLayer(id: LayerId, transform: (TimelineLayer) -> TimelineLayer) {
        val comp = activeComp ?: return
        if (comp.layers.none { it.id == id }) return
        val changed = comp.copy(layers = comp.layers.map { if (it.id == id) transform(it) else it })
        commit(document.copy(compositions = document.compositions.map { if (it.id == comp.id) changed else it }))
    }
    fun renameLayer(id: LayerId, name: String) = updateLayer(id) { it.copy(name = name) }
    fun removeLayer(id: LayerId) {
        val comp = activeComp ?: return
        commit(document.copy(compositions = document.compositions.map {
            if (it.id == comp.id) it.copy(layers = it.layers.filterNot { layer -> layer.id == id }) else it
        }))
        if (selectedLayer == id) selectedLayer = null
    }
    fun moveLayer(id: LayerId, newIndex: Int) {
        val comp = activeComp ?: return
        val layers = comp.layers.toMutableList()
        val oldIndex = layers.indexOfFirst { it.id == id }
        if (oldIndex < 0) return
        val layer = layers.removeAt(oldIndex)
        layers.add(newIndex.coerceIn(0, layers.size), layer)
        commit(document.copy(compositions = document.compositions.map { if (it.id == comp.id) it.copy(layers = layers) else it }))
    }
    fun undo(): Boolean {
        if (!canUndo) return false
        redoStack.addLast(document)
        document = undoStack.removeLast()
        selectedLayer = selectedLayer?.takeIf { id -> activeComp?.layers?.any { it.id == id } == true }
        return true
    }
    fun redo(): Boolean {
        if (!canRedo) return false
        undoStack.addLast(document)
        document = redoStack.removeLast()
        selectedLayer = selectedLayer?.takeIf { id -> activeComp?.layers?.any { it.id == id } == true }
        return true
    }
}
