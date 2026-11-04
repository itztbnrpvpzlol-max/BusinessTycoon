package com.aetherlauncher.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val bg = Color(0xFF090D16)
private val panel = Color(0xFF131B2A)
private val accent = Color(0xFF7C9CFF)
private val muted = Color(0xFF9AA9C2)
private const val PREFS = "aether_settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = accent, background = bg, surface = panel)) {
                AetherHome()
            }
        }
    }
}

private data class DeviceInfo(val model: String, val android: String, val ramMb: Long, val appClass: Int, val is64: Boolean, val abis: String, val glEs: String)

private fun detectDevice(context: Context): DeviceInfo {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mem = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mem)
    val glVersion = am.deviceConfigurationInfo.reqGlEsVersion
    val glVersionText = (glVersion shr 16).toString() + "." + (glVersion and 0xffff).toString()
    return DeviceInfo(
        Build.MANUFACTURER + " " + Build.MODEL,
        Build.VERSION.RELEASE ?: "Unknown",
        mem.totalMem / (1024L * 1024L),
        am.memoryClass,
        Process.is64Bit(),
        Build.SUPPORTED_ABIS.joinToString(),
        glVersionText
    )
}

@Composable
private fun AetherHome() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val device = remember { detectDevice(context) }
    var tab by remember { mutableStateOf("Home") }
    var version by remember { mutableStateOf(prefs.getString("version", "1.8.9") ?: "1.8.9") }
    var ram by remember { mutableIntStateOf(prefs.getInt("ram", 512).coerceIn(384, 768)) }
    var chunks by remember { mutableIntStateOf(prefs.getInt("chunks", 2).coerceIn(2, 6)) }
    var fps by remember { mutableIntStateOf(prefs.getInt("fps", 30).coerceIn(30, 60)) }

    Surface(Modifier.fillMaxSize(), color = bg) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("AETHER", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("JAVA EDITION • ANDROID", color = muted, fontSize = 10.sp, letterSpacing = 2.sp)
                when (tab) {
                    "Home" -> {
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(22.dp)) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Minecraft: Java Edition", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                Text("Selected version: $version", color = muted)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("1.8.9", "1.12.2", "1.16.5").forEach {
                                        TextButton(onClick = { version = it; prefs.edit().putString("version", it).apply() }) {
                                            Text(it, color = if (version == it) accent else muted)
                                        }
                                    }
                                }
                                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                                    Text("GAME ENGINE NOT CONNECTED")
                                }
                                Text("The Java runtime, game downloader and renderer are not integrated yet.", color = muted, fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, null, tint = accent)
                            Text("  LOW-END PERFORMANCE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Tune("Java memory", "$ram MB", ram.toFloat(), 384f..768f, 5) {
                                    ram = it.toInt(); prefs.edit().putInt("ram", ram).apply()
                                }
                                HorizontalDivider()
                                Tune("Render distance", "$chunks chunks", chunks.toFloat(), 2f..6f, 3) {
                                    chunks = it.toInt(); prefs.edit().putInt("chunks", chunks).apply()
                                }
                                HorizontalDivider()
                                Tune("FPS cap", "$fps FPS", fps.toFloat(), 30f..60f, 1) {
                                    fps = it.toInt(); prefs.edit().putInt("fps", fps).apply()
                                }
                                Button(onClick = {
                                    ram = 512; chunks = 2; fps = 30
                                    prefs.edit().putInt("ram", 512).putInt("chunks", 2).putInt("fps", 30).apply()
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("APPLY MY PHONE PRESET")
                                }
                                Text("Conservative starting values for a 2 GB device. Actual game performance remains untested.", color = muted, fontSize = 11.sp)
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("DEVICE DETECTED", color = accent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(device.model, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(device.ramMb.toString() + " MB RAM • Android " + device.android, color = muted, fontSize = 12.sp)
                                Text(if (device.is64) "64-bit app process" else "32-bit app process", color = muted, fontSize = 12.sp)
                            }
                        }
                    }
                    "Instances" -> {
                        Text("INSTANCES", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Default profile", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Minecraft Java Edition • $version", color = muted)
                                Text("Local profile settings are saved. Game installation is not available yet.", color = muted, fontSize = 12.sp)
                            }
                        }
                    }
                    else -> {
                        Text("DEVICE & COMPATIBILITY", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Info("Model", device.model)
                                Info("Android", device.android)
                                Info("Total RAM", device.ramMb.toString() + " MB")
                                Info("Android app memory class", device.appClass.toString() + " MB")
                                Info("Process architecture", if (device.is64) "64-bit" else "32-bit")
                                Info("Supported ABIs", device.abis)
                                Info("OpenGL ES reported", device.glEs)
                            }
                        }
                        Text("Your reported Mali-T820 is the graphics target. We still need a live EGL graphics probe to read the installed GPU driver.", color = muted, fontSize = 12.sp)
                    }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().background(panel), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Home", "Instances", "Settings").forEach { name ->
                    TextButton(onClick = { tab = name }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(when (name) {
                                "Home" -> Icons.Default.Gamepad
                                "Instances" -> Icons.Default.Memory
                                else -> Icons.Default.Settings
                            }, contentDescription = name, tint = if (tab == name) accent else muted)
                            Text(name, color = if (tab == name) accent else muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Info(label: String, value: String) {
    Column {
        Text(label.uppercase(), color = muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun Tune(label: String, valueText: String, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int, change: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(valueText, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Slider(value = value, onValueChange = change, valueRange = range, steps = steps)
    }
}
