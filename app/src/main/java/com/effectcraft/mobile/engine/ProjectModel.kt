package com.effectcraft.mobile.engine

/** Kotlin-native document model. Item, layer and property IDs share one monotonic namespace. */
@JvmInline value class ItemId(val value: Long)
@JvmInline value class LayerId(val value: Long)
@JvmInline value class PropertyId(val value: Long)

enum class LayerKind { FOOTAGE, COMPOSITION, SOLID, TEXT, SHAPE, NULL, CAMERA, LIGHT, MODEL, PRIMITIVE }
enum class LayerQuality { BEST, DRAFT, WIREFRAME }
enum class BlendMode { NORMAL, MULTIPLY, SCREEN, OVERLAY, ADD, DIFFERENCE }

data class TransformProperties(
    val anchorPoint: List<Double> = listOf(0.0, 0.0, 0.0),
    val position: List<Double> = listOf(0.0, 0.0, 0.0),
    val scale: List<Double> = listOf(100.0, 100.0, 100.0),
    val orientation: List<Double> = listOf(0.0, 0.0, 0.0),
    val rotation: Double = 0.0,
    val opacity: Double = 100.0
)
data class LayerSwitches(
    val video: Boolean = true,
    val audio: Boolean = true,
    val solo: Boolean = false,
    val locked: Boolean = false,
    val shy: Boolean = false,
    val effects: Boolean = true,
    val motionBlur: Boolean = false,
    val threeD: Boolean = false,
    val quality: LayerQuality = LayerQuality.BEST
)
data class TimelineLayer(
    val id: LayerId,
    val name: String,
    val kind: LayerKind,
    val startTime: Tick = Tick.ZERO,
    val inPoint: Tick = Tick.ZERO,
    val outPoint: Tick,
    val transform: TransformProperties = TransformProperties(),
    val switches: LayerSwitches = LayerSwitches(),
    val blendMode: BlendMode = BlendMode.NORMAL,
    val properties: Map<String, KeyframeTrack> = emptyMap()
) {
    fun contains(time: Tick): Boolean = time >= startTime + inPoint && time < startTime + outPoint
}
data class Composition(
    val id: ItemId,
    val name: String,
    val width: Int,
    val height: Int,
    val frameRate: FrameRate,
    val duration: Tick,
    val background: List<Double> = listOf(0.0, 0.0, 0.0),
    /** Layer 1 is the topmost layer, matching EffectCraft's project model. */
    val layers: List<TimelineLayer> = emptyList()
) {
    init { require(width in 1..16384 && height in 1..16384); require(background.size == 3) }
}
data class ProjectDocument(
    val schema: Int = 2,
    val name: String = "Untitled Project",
    val compositions: List<Composition> = emptyList(),
    val nextId: Long = 1L
) {
    init { require(schema > 0 && nextId > 0) }
    fun allocateId(): Pair<Long, ProjectDocument> = nextId to copy(nextId = Math.addExact(nextId, 1L))
}
