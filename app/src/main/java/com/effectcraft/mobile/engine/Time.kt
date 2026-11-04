package com.effectcraft.mobile.engine

/** EffectCraft's integer project timebase: 254,016,000,000 ticks per second. */
@JvmInline
value class Tick(val value: Long) : Comparable<Tick> {
    override fun compareTo(other: Tick): Int = value.compareTo(other.value)
    operator fun plus(other: Tick) = Tick(Math.addExact(value, other.value))
    operator fun minus(other: Tick) = Tick(Math.subtractExact(value, other.value))
    companion object {
        const val TICKS_PER_SECOND: Long = 254_016_000_000L
        val ZERO = Tick(0L)
        fun fromSeconds(seconds: Double) = Tick((seconds * TICKS_PER_SECOND).toLong())
    }
    fun seconds(): Double = value.toDouble() / TICKS_PER_SECOND
}

data class FrameRate(val numerator: Int, val denominator: Int = 1) {
    init { require(numerator > 0 && denominator > 0) }
    val framesPerSecond: Double get() = numerator.toDouble() / denominator
    fun frameToTick(frame: Long) = Tick((frame.toDouble() * Tick.TICKS_PER_SECOND * denominator / numerator).toLong())
    fun tickToFrame(tick: Tick) = kotlin.math.floor(tick.value.toDouble() * numerator / (Tick.TICKS_PER_SECOND.toDouble() * denominator)).toLong()
    companion object {
        val FPS_24 = FrameRate(24)
        val FPS_25 = FrameRate(25)
        val FPS_30 = FrameRate(30)
        val FPS_60 = FrameRate(60)
    }
}
