package com.businesstycoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val bg = Color(0xFF111827)
private val panel = Color(0xFF1F2937)
private val green = Color(0xFF34D399)
private val muted = Color(0xFF9CA3AF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var game by remember { mutableStateOf(Storage.load(this)) }
            var tab by remember { mutableIntStateOf(0) }
            var dialog by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                while (true) { delay(1000); game = Engine.tick(game) }
            }
            LaunchedEffect(game) { Storage.save(this@MainActivity, game) }
            MaterialTheme(colorScheme = darkColorScheme(primary = green, background = bg, surface = panel, onPrimary = bg)) {
                Scaffold(containerColor = bg, bottomBar = {
                    NavigationBar(containerColor = panel) {
                        val labels = listOf("Home", "Businesses", "Invest", "Upgrades", "Stats")
                        val icons = listOf(Icons.Default.Home, Icons.Default.Store, Icons.Default.ShowChart, Icons.Default.Upgrade, Icons.Default.BarChart)
                        labels.forEachIndexed { i, label ->
                            NavigationBarItem(selected = tab == i, onClick = { tab = i },
                                icon = { Icon(icons[i], contentDescription = label) }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("BUSINESS TYCOON", color = muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Text(cash(game.cash), color = green, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Surface(color = Color(0xFF243B35), shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Text("INCOME / SEC", color = muted, fontSize = 9.sp)
                                    Text(cash(game.passiveIncome), color = green, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        when (tab) {
                            0 -> Home(game, onTap = { game = game.copy(cash = game.cash + game.tapValue, lifetime = game.lifetime + game.tapValue, taps = game.taps + 1) }, onShop = { tab = 1 })
                            1 -> BusinessPage(game) { game = Engine.buyBusiness(game, it) }
                            2 -> InvestPage(game,
                                buyShare = { if (game.cash >= game.stockPrice) game = game.copy(cash = game.cash - game.stockPrice, shares = game.shares + 1) },
                                sellShare = { if (game.shares > 0) game = game.copy(cash = game.cash + game.stockPrice, shares = game.shares - 1) },
                                buyProperty = { val price = Engine.propertyCost(game.properties); if (game.cash >= price) game = game.copy(cash = game.cash - price, properties = game.properties + 1) })
                            3 -> UpgradePage(game,
                                click = { val p = Engine.clickCost(game.clickLevel); if (game.cash >= p) game = game.copy(cash = game.cash - p, clickLevel = game.clickLevel + 1) },
                                income = { val p = Engine.incomeCost(game.incomeLevel); if (game.cash >= p) game = game.copy(cash = game.cash - p, incomeLevel = game.incomeLevel + 1) })
                            else -> StatsPage(game) {
                                if (game.lifetime >= 1_000_000) game = GameData(prestige = game.prestige + 1, lifetime = game.lifetime)
                                else dialog = "Earn $1M lifetime cash to unlock prestige."
                            }
                        }
                    }
                    if (dialog.isNotEmpty()) AlertDialog(onDismissRequest = { dialog = "" }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("OK") } }, text = { Text(dialog) })
                }
            }
        }
    }
}

@Composable private fun Heading(title: String, sub: String) {
    Column(Modifier.padding(bottom = 14.dp)) {
        Text(title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(sub, color = muted, fontSize = 12.sp)
    }
}
@Composable private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = panel, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp)) { Text(label, color = muted, fontSize = 10.sp); Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1) }
    }
}
@Composable private fun CardRow(content: @Composable RowScope.() -> Unit) {
    Surface(color = panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable private fun Home(g: GameData, onTap: () -> Unit, onShop: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Tile("Businesses", g.owned.sum().toString(), Modifier.weight(1f))
            Tile("Prestige", "⭐ ${g.prestige}", Modifier.weight(1f))
            Tile("Per tap", cash(g.tapValue), Modifier.weight(1f))
        }
        Spacer(Modifier.height(35.dp))
        Text("BUILD YOUR EMPIRE", color = muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(18.dp))
        Surface(onClick = onTap, modifier = Modifier.size(220.dp), shape = CircleShape, color = Color(0xFF174C3A), border = BorderStroke(5.dp, green), shadowElevation = 10.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("💵", fontSize = 58.sp); Text("TAP FOR CASH", fontWeight = FontWeight.ExtraBold); Text("+${cash(g.tapValue)}", color = green, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(25.dp))
        Text("Your businesses earn money every second.", color = muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onShop) { Icon(Icons.Default.Store, null); Spacer(Modifier.width(8.dp)); Text("EXPAND YOUR BUSINESS") }
        Spacer(Modifier.weight(1f))
        Text("Tap • Build • Upgrade • Become a tycoon", color = muted, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
    }
}
@Composable private fun BusinessPage(g: GameData, buy: (Int) -> Unit) {
    Column {
        Heading("Your Businesses", "Buy businesses to generate automatic income.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            itemsIndexed(businesses) { i, b ->
                val count = g.owned[i]; val price = Engine.cost(i, count)
                CardRow {
                    Text(b.emoji, fontSize = 29.sp)
                    Column(Modifier.weight(1f)) {
                        Text(b.name, fontWeight = FontWeight.Bold)
                        Text(b.info, color = muted, fontSize = 11.sp)
                        Text("Owned: $count • ${cash(count * b.income * (1 + g.incomeLevel * .25) * g.multiplier)}/sec", color = green, fontSize = 11.sp)
                    }
                    Button(onClick = { buy(i) }, enabled = g.cash >= price, contentPadding = PaddingValues(horizontal = 10.dp)) { Text(cash(price), fontSize = 11.sp) }
                }
            }
        }
    }
}
@Composable private fun InvestPage(g: GameData, buyShare: () -> Unit, sellShare: () -> Unit, buyProperty: () -> Unit) {
    Column {
        Heading("Investments", "Put your cash to work outside your businesses.")
        CardRow {
            Text("📈", fontSize = 32.sp)
            Column(Modifier.weight(1f)) {
                Text("Market shares", fontWeight = FontWeight.Bold)
                Text("Price: ${cash(g.stockPrice)} • Owned: ${g.shares}", color = muted, fontSize = 12.sp)
                Text("Income: ${cash(g.shares * g.stockPrice * .0005)}/sec", color = green, fontSize = 11.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = buyShare, enabled = g.cash >= g.stockPrice, modifier = Modifier.weight(1f)) { Text("Buy 1 share") }
            OutlinedButton(onClick = sellShare, enabled = g.shares > 0, modifier = Modifier.weight(1f)) { Text("Sell 1 share") }
        }
        Spacer(Modifier.height(12.dp))
        CardRow {
            Text("🏠", fontSize = 32.sp)
            Column(Modifier.weight(1f)) {
                Text("Rental property", fontWeight = FontWeight.Bold)
                Text("Owned: ${g.properties}", color = muted, fontSize = 12.sp)
                Text("+$18.00 passive income / sec each", color = green, fontSize = 11.sp)
                Text("Next: ${cash(Engine.propertyCost(g.properties))}", color = muted, fontSize = 11.sp)
            }
            Button(onClick = buyProperty, enabled = g.cash >= Engine.propertyCost(g.properties)) { Text("Buy") }
        }
        Spacer(Modifier.height(12.dp))
        Text("Share prices move over time. Investments use in-game cash only.", color = muted, fontSize = 11.sp)
    }
}
@Composable private fun UpgradePage(g: GameData, click: () -> Unit, income: () -> Unit) {
    Column {
        Heading("Upgrades", "Improve your earning power.")
        CardRow {
            Text("👆", fontSize = 32.sp)
            Column(Modifier.weight(1f)) {
                Text("Stronger clicks", fontWeight = FontWeight.Bold)
                Text("Level ${g.clickLevel} • Tap: ${cash(g.tapValue)}", color = muted, fontSize = 12.sp)
                Text("Adds $2 base value per tap.", color = green, fontSize = 11.sp)
                Text("Cost: ${cash(Engine.clickCost(g.clickLevel))}", color = muted, fontSize = 11.sp)
            }
            Button(onClick = click, enabled = g.cash >= Engine.clickCost(g.clickLevel)) { Text("Upgrade") }
        }
        Spacer(Modifier.height(10.dp))
        CardRow {
            Text("⚙️", fontSize = 32.sp)
            Column(Modifier.weight(1f)) {
                Text("Business efficiency", fontWeight = FontWeight.Bold)
                Text("Level ${g.incomeLevel}", color = muted, fontSize = 12.sp)
                Text("Increases business income by 25%.", color = green, fontSize = 11.sp)
                Text("Cost: ${cash(Engine.incomeCost(g.incomeLevel))}", color = muted, fontSize = 11.sp)
            }
            Button(onClick = income, enabled = g.cash >= Engine.incomeCost(g.incomeLevel)) { Text("Upgrade") }
        }
    }
}
@Composable private fun StatsPage(g: GameData, prestige: () -> Unit) {
    Column {
        Heading("Empire Statistics", "Your progress so far.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("Lifetime earned", cash(g.lifetime), Modifier.weight(1f))
            Tile("Total taps", g.taps.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("Businesses", g.owned.sum().toString(), Modifier.weight(1f))
            Tile("Income / sec", cash(g.passiveIncome), Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        CardRow {
            Text("⭐", fontSize = 32.sp)
            Column(Modifier.weight(1f)) {
                Text("Prestige", fontWeight = FontWeight.Bold)
                Text("Reset cash, businesses and upgrades for a permanent +25% income bonus.", color = muted, fontSize = 12.sp)
                Text("Requirement: $1M lifetime earned", color = green, fontSize = 11.sp)
                Text("Current bonus: +${g.prestige * 25}%", color = green, fontWeight = FontWeight.Bold)
            }
        }
        Button(onClick = prestige, enabled = g.lifetime >= 1_000_000, modifier = Modifier.fillMaxWidth()) { Text("PRESTIGE & START AGAIN") }
        Spacer(Modifier.height(12.dp))
        Text("Progress saves automatically. Offline earnings are credited when you return.", color = muted, fontSize = 11.sp)
    }
}
