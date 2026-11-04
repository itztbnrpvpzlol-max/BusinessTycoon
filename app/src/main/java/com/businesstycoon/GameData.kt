package com.businesstycoon

import android.content.Context
import kotlin.math.pow

data class Business(val name: String, val emoji: String, val info: String, val cost: Double, val income: Double)
val businesses = listOf(
    Business("Lemonade Stand", "🍋", "A small start with big potential.", 25.0, 1.0),
    Business("Coffee Shop", "☕", "Serve the morning rush.", 180.0, 6.0),
    Business("Corner Store", "🏪", "Everyday essentials.", 1200.0, 32.0),
    Business("Restaurant", "🍔", "Turn hungry customers into profit.", 8500.0, 190.0),
    Business("Supermarket", "🛒", "Bigger store, bigger returns.", 55000.0, 1150.0),
    Business("Factory", "🏭", "Scale production.", 400000.0, 7200.0),
    Business("Bank", "🏦", "Make money work for you.", 3000000.0, 48000.0),
    Business("Hotel", "🏨", "Premium stays, premium profits.", 25000000.0, 320000.0)
)
data class GameData(
    val cash: Double = 50.0, val owned: List<Int> = List(businesses.size) { 0 },
    val clickLevel: Int = 0, val incomeLevel: Int = 0, val shares: Int = 0,
    val properties: Int = 0, val stockPrice: Double = 100.0, val prestige: Int = 0,
    val lifetime: Double = 0.0, val taps: Long = 0, val savedAt: Long = System.currentTimeMillis()
) {
    val multiplier get() = 1.0 + prestige * 0.25
    val tapValue get() = (1.0 + clickLevel * 2.0) * multiplier
    val businessIncome get() = businesses.indices.sumOf { owned[it] * businesses[it].income * (1 + incomeLevel * 0.25) * multiplier }
    val passiveIncome get() = businessIncome + properties * 18.0 + shares * stockPrice * 0.0005
}
object Engine {
    fun cost(i: Int, count: Int) = businesses[i].cost * 1.15.pow(count)
    fun clickCost(level: Int) = 100.0 * 2.2.pow(level)
    fun incomeCost(level: Int) = 500.0 * 2.5.pow(level)
    fun propertyCost(count: Int) = 25000.0 * 1.8.pow(count)
    fun buyBusiness(g: GameData, i: Int): GameData {
        val price = cost(i, g.owned[i]); if (g.cash < price) return g
        val list = g.owned.toMutableList(); list[i]++
        return g.copy(cash = g.cash - price, owned = list)
    }
    fun tick(g: GameData): GameData {
        val earned = g.passiveIncome
        return g.copy(cash = g.cash + earned, lifetime = g.lifetime + earned,
            stockPrice = (g.stockPrice * (1.0 + (-2..3).random() / 1000.0)).coerceAtLeast(10.0))
    }
    fun offline(g: GameData): Pair<GameData, Double> {
        val seconds = ((System.currentTimeMillis() - g.savedAt) / 1000).coerceIn(0, 28800)
        val earned = seconds * g.passiveIncome
        return g.copy(cash = g.cash + earned, lifetime = g.lifetime + earned, savedAt = System.currentTimeMillis()) to earned
    }
}
object Storage {
    private const val KEY = "tycoon_save"
    fun load(c: Context): GameData {
        val p = c.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        val g = GameData(cash = p.getFloat("cash", 50f).toDouble(),
            owned = businesses.indices.map { p.getInt("owned_$it", 0) },
            clickLevel = p.getInt("click", 0), incomeLevel = p.getInt("income", 0),
            shares = p.getInt("shares", 0), properties = p.getInt("properties", 0),
            stockPrice = p.getFloat("stock", 100f).toDouble(), prestige = p.getInt("prestige", 0),
            lifetime = p.getFloat("lifetime", 0f).toDouble(), taps = p.getLong("taps", 0),
            savedAt = p.getLong("savedAt", System.currentTimeMillis()))
        return Engine.offline(g).first
    }
    fun save(c: Context, g: GameData) {
        val e = c.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit()
            .putFloat("cash", g.cash.toFloat()).putInt("click", g.clickLevel).putInt("income", g.incomeLevel)
            .putInt("shares", g.shares).putInt("properties", g.properties).putFloat("stock", g.stockPrice.toFloat())
            .putInt("prestige", g.prestige).putFloat("lifetime", g.lifetime.toFloat())
            .putLong("taps", g.taps).putLong("savedAt", System.currentTimeMillis())
        g.owned.forEachIndexed { i, n -> e.putInt("owned_$i", n) }
        e.apply()
    }
}
fun cash(n: Double): String = when {
    n >= 1e12 -> "$" + "%.2fT".format(n / 1e12)
    n >= 1e9 -> "$" + "%.2fB".format(n / 1e9)
    n >= 1e6 -> "$" + "%.2fM".format(n / 1e6)
    n >= 1e3 -> "$" + "%.2fK".format(n / 1e3)
    else -> "$" + "%.2f".format(n)
}
