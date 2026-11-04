package com.effectcraft.mobile.engine

import kotlin.math.abs
import kotlin.math.pow

/** Port of EffectCraft's temporal keyframe semantics: linear, Bezier and hold. */
enum class Interpolation { LINEAR, BEZIER, HOLD }
data class TemporalEase(val speed: Double = 0.0, val influence: Double = 1.0 / 6.0) {
    fun boundedInfluence() = influence.coerceIn(0.0, 1.0)
    companion object { val EASY = TemporalEase(0.0, 1.0 / 3.0) }
}
data class Keyframe(
    val time: Tick,
    val value: List<Double>,
    val inInterpolation: Interpolation = Interpolation.LINEAR,
    val outInterpolation: Interpolation = Interpolation.LINEAR,
    val inEase: List<TemporalEase> = emptyList(),
    val outEase: List<TemporalEase> = emptyList(),
    val muted: Boolean = false
) { init { require(value.isNotEmpty()) } }

object KeyframeEvaluator {
    fun evaluate(input: List<Keyframe>, time: Tick): List<Double>? {
        val keys = input.asSequence().filterNot { it.muted }.sortedBy { it.time.value }.toList()
        if (keys.isEmpty()) return null
        if (time <= keys.first().time) return keys.first().value
        if (time >= keys.last().time) return keys.last().value
        val rightIndex = keys.indexOfFirst { it.time >= time }
        if (rightIndex <= 0) return keys.first().value
        val left = keys[rightIndex - 1]
        val right = keys[rightIndex]
        if (left.outInterpolation == Interpolation.HOLD) return left.value
        val durationTicks = (right.time - left.time).value
        if (durationTicks <= 0L) return right.value
        val x = ((time - left.time).value.toDouble() / durationTicks).coerceIn(0.0, 1.0)
        val dimensions = minOf(left.value.size, right.value.size)
        return List(dimensions) { dimension ->
            val a = left.value[dimension]
            val b = right.value[dimension]
            val progress = if (left.outInterpolation == Interpolation.BEZIER || right.inInterpolation == Interpolation.BEZIER) {
                val outgoing = left.outEase.getOrElse(dimension) { TemporalEase() }
                val incoming = right.inEase.getOrElse(dimension) { TemporalEase() }
                val delta = b - a
                if (abs(delta) < 1e-12) x else {
                    val seconds = durationTicks.toDouble() / Tick.TICKS_PER_SECOND
                    easeProgress(outgoing.speed * seconds / delta, outgoing.boundedInfluence(),
                        incoming.speed * seconds / delta, incoming.boundedInfluence(), x)
                }
            } else x
            a + (b - a) * progress
        } + if (left.value.size > dimensions) left.value.drop(dimensions) else emptyList()
    }

    /** Cubic time-handle inversion followed by value-handle evaluation, as in EffectCraft. */
    fun easeProgress(outSpeed: Double, outInfluence: Double, inSpeed: Double, inInfluence: Double, x: Double): Double {
        val i0 = outInfluence.coerceIn(0.0, 1.0)
        val i1 = inInfluence.coerceIn(0.0, 1.0)
        val u = solveBezierTime(i0, 1.0 - i1, x)
        return cubic(0.0, outSpeed * i0, 1.0 - inSpeed * i1, 1.0, u)
    }
    private fun solveBezierTime(x1: Double, x2: Double, target: Double): Double {
        var low = 0.0
        var high = 1.0
        repeat(56) {
            val mid = (low + high) * 0.5
            if (cubic(0.0, x1, x2, 1.0, mid) < target) low = mid else high = mid
        }
        return (low + high) * 0.5
    }
    private fun cubic(p0: Double, p1: Double, p2: Double, p3: Double, t: Double): Double {
        val u = 1.0 - t
        return u.pow(3) * p0 + 3.0 * u * u * t * p1 + 3.0 * u * t * t * p2 + t.pow(3) * p3
    }
}

/** Editable property track; setting a value on an animated track creates/replaces a key. */
class KeyframeTrack(initialValue: List<Double>) {
    var staticValue: List<Double> = initialValue.toList()
        private set
    private val mutableKeys = mutableListOf<Keyframe>()
    val keys: List<Keyframe> get() = mutableKeys.toList()
    val animated: Boolean get() = mutableKeys.isNotEmpty()
    fun valueAt(time: Tick): List<Double> = if (mutableKeys.isEmpty()) staticValue
        else KeyframeEvaluator.evaluate(mutableKeys, time) ?: staticValue
    fun setValueAt(time: Tick, value: List<Double>, holdOnly: Boolean = false) {
        require(value.isNotEmpty())
        if (mutableKeys.isEmpty()) { staticValue = value.toList(); return }
        val index = mutableKeys.indexOfFirst { it.time == time }
        val mode = if (holdOnly) Interpolation.HOLD else Interpolation.LINEAR
        val key = Keyframe(time, value.toList(), mode, mode)
        if (index >= 0) mutableKeys[index] = key else mutableKeys.add(key)
        mutableKeys.sortBy { it.time.value }
    }
    fun setAnimated(enabled: Boolean, time: Tick) {
        if (enabled && mutableKeys.isEmpty()) mutableKeys += Keyframe(time, staticValue)
        if (!enabled && mutableKeys.isNotEmpty()) { staticValue = valueAt(time); mutableKeys.clear() }
    }
    fun replaceKeys(keys: List<Keyframe>) {
        mutableKeys.clear()
        mutableKeys.addAll(keys.sortedBy { it.time.value })
    }
}
