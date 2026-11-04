package com.aetherlauncher.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF090D16)
private val Panel = Color(0xFF131B2A)
private val PanelLight = Color(0xFF1B2639)
private val Accent = Color(0xFF7C9CFF)
private val Muted = Color(0xFF9AA9C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AetherTheme { LauncherHome() } }
    }
}

@Composable
private fun AetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            secondary = Color(0xFF66D6C2),
            background = Background,
            surface = Panel,
            onPrimary = Color(0xFF08101F),
            onBackground = Color(0xFFF4F7FF),
            onSurface = Color(0xFFF4F7FF)
        ),
        content = content
    )
}

@Composable
private fun LauncherHome() {
    val versions = listOf("1.21.4", "1.20.1", "1.19.4", "1.16.5", "1.12.2", "1.8.9")
    var selectedVersion by remember { mutableStateOf(versions.first()) }
    var versionMenu by remember { mutableStateOf(false) }
    var memory by remember { mutableFloatStateOf(4f) }
    var renderDistance by remember { mutableFloatStateOf(8f) }
    var fpsLimit by remember { mutableFloatStateOf(60f) }
    var selectedTab by remember { mutableStateOf("Home") }

    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("AETHER", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                        Text("JAVA EDITION • ANDROID", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                    }
                    Surface(color = Color(0xFF1D3150), shape = RoundedCornerShape(50)) {
                        Text("PREVIEW  0.1", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gamepad, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
                            Text("  YOUR GAME", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        }
                        Text("Minecraft: Java Edition", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Choose a version to prepare your game profile.", color = Muted, fontSize = 13.sp)
                        TextButton(onClick = { versionMenu = true }) {
                            Text("VERSION   $selectedVersion  ▾", color = Accent, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = versionMenu, onDismissRequest = { versionMenu = false }) {
                            versions.forEach { version ->
                                DropdownMenuItem(
                                    text = { Text(version) },
                                    onClick = { selectedVersion = version; versionMenu = false }
                                )
                            }
                        }
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF34415A), disabledContentColor = Color(0xFFB9C6DB))
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null)
                            Text("  GAME ENGINE NOT CONNECTED", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text("The launcher interface is ready. Java runtime and graphics integration are the next development milestone.", color = Muted, fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, tint = Accent, contentDescription = null)
                    Text("  PERFORMANCE", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingSlider("Memory allocation", "${memory.toInt()} GB", memory, 1f..8f) { memory = it }
                        HorizontalDivider(color = PanelLight)
                        SettingSlider("Render distance", "${renderDistance.toInt()} chunks", renderDistance, 2f..16f) { renderDistance = it }
                        HorizontalDivider(color = PanelLight)
                        SettingSlider("FPS limit", "${fpsLimit.toInt()} FPS", fpsLimit, 30f..120f) { fpsLimit = it }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PanelLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, tint = Color(0xFF66D6C2), contentDescription = null, modifier = Modifier.size(25.dp))
                        Column {
                            Text("Runtime status", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("No Java runtime installed yet", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider(color = PanelLight)
            Row(
                modifier = Modifier.fillMaxWidth().background(Panel).padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Home", "Instances", "Settings").forEach { tab ->
                    TextButton(onClick = { selectedTab = tab }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (tab) {
                                    "Home" -> Icons.Default.Gamepad
                                    "Instances" -> Icons.Default.CloudDownload
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = tab,
                                tint = if (selectedTab == tab) Accent else Muted
                            )
                            Text(tab, color = if (selectedTab == tab) Accent else Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = Color.White, fontSize = 13.sp)
            Text(valueLabel, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}
