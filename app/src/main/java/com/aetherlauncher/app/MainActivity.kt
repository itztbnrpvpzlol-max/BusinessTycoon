package com.aetherlauncher.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
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
private data class GraphicsInfo(val version: String, val renderer: String, val vendor: String, val error: String? = null)
private data class McVersion(val id: String, val type: String, val releaseTime: String, val url: String)

private suspend fun fetchReleaseCatalog(): List<McVersion> = withContext(Dispatchers.IO) {
    val connection = (URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").openConnection() as HttpURLConnection).apply {
        connectTimeout = 12000
        readTimeout = 12000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "AetherLauncher/0.1.0")
    }
    try {
        if (connection.responseCode !in 200..299) throw IllegalStateException("Mojang returned HTTP ${connection.responseCode}")
        val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val versions = json.getJSONArray("versions")
        buildList {
            for (i in 0 until versions.length()) {
                val item = versions.getJSONObject(i)
                if (item.optString("type") == "release") {
                    add(McVersion(
                        item.getString("id"),
                        item.getString("type"),
                        item.optString("releaseTime", ""),
                        item.getString("url")
                    ))
                }
                if (size >= 40) break
            }
        }
    } finally {
        connection.disconnect()
    }
}

private fun probeGraphics(): GraphicsInfo {
    var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    var context: EGLContext = EGL14.EGL_NO_CONTEXT
    var surface: EGLSurface = EGL14.EGL_NO_SURFACE
    try {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) throw IllegalStateException("No EGL display")
        val versions = IntArray(2)
        if (!EGL14.eglInitialize(display, versions, 0, versions, 1)) throw IllegalStateException("EGL initialize failed")
        val configAttrs = intArrayOf(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_NONE)
        val configs = arrayOfNulls<EGLConfig>(1)
        val count = IntArray(1)
        if (!EGL14.eglChooseConfig(display, configAttrs, 0, configs, 0, 1, count, 0) || count[0] == 0) throw IllegalStateException("No compatible EGL config")
        val contextAttrs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttrs, 0)
        if (context == EGL14.EGL_NO_CONTEXT) throw IllegalStateException("EGL context creation failed")
        val surfaceAttrs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttrs, 0)
        if (surface == EGL14.EGL_NO_SURFACE) throw IllegalStateException("EGL surface creation failed")
        if (!EGL14.eglMakeCurrent(display, surface, surface, context)) throw IllegalStateException("EGL make-current failed")
        return GraphicsInfo(
            GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown",
            GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown",
            GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
        )
    } catch (e: Exception) {
        return GraphicsInfo("Unavailable", "Unavailable", "Unavailable", e.message ?: "Graphics probe failed")
    } finally {
        if (display != EGL14.EGL_NO_DISPLAY) {
            if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface)
            if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
            EGL14.eglTerminate(display)
        }
    }
}


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
    var graphics by remember { mutableStateOf<GraphicsInfo?>(null) }
    LaunchedEffect(Unit) { graphics = withContext(Dispatchers.Default) { probeGraphics() } }
    var tab by remember { mutableStateOf("Home") }
    var version by remember { mutableStateOf(prefs.getString("version", "1.8.9") ?: "1.8.9") }
    var ram by remember { mutableIntStateOf(prefs.getInt("ram", 512).coerceIn(384, 768)) }
    var chunks by remember { mutableIntStateOf(prefs.getInt("chunks", 2).coerceIn(2, 6)) }
    var fps by remember { mutableIntStateOf(prefs.getInt("fps", 30).coerceIn(30, 60)) }
    var catalog by remember { mutableStateOf<List<McVersion>>(emptyList()) }
    var catalogLoading by remember { mutableStateOf(false) }
    var catalogError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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

                    "Studio" -> {
                        Text("AETHER STUDIO", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("EffectCraft browser workspace • experimental", color = muted, fontSize = 12.sp)
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Before you start", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("This opens a community-hosted EffectCraft web build, not an official Android port. It is an older alpha release and may fail on this phone's GPU or run slowly with 2 GB RAM. Save or export projects often.", color = muted, fontSize = 11.sp)
                            }
                        }
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().height(560.dp),
                            factory = { viewContext ->
                                WebView(viewContext).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.databaseEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.allowFileAccess = false
                                    settings.allowContentAccess = false
                                    webViewClient = WebViewClient()
                                    webChromeClient = WebChromeClient()
                                    loadUrl("https://getartcraft.org/studio/effectcraft?webgl")
                                }
                            },
                            update = { webView ->
                                if (webView.url.isNullOrBlank()) {
                                    webView.loadUrl("https://getartcraft.org/studio/effectcraft?webgl")
                                }
                            }
                        )
                    }
                    "Instances" -> {
                        Text("INSTANCES & VERSIONS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Default profile", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Minecraft Java Edition • $version", color = muted)
                                Text("Choose a release below to set the profile version. This catalog reads official Mojang metadata only; it does not download game files or install Minecraft.", color = muted, fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    catalogLoading = true
                                    catalogError = null
                                    try {
                                        catalog = fetchReleaseCatalog()
                                    } catch (e: Exception) {
                                        catalogError = e.message ?: "Could not load official version list"
                                    } finally {
                                        catalogLoading = false
                                    }
                                }
                            },
                            enabled = !catalogLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (catalogLoading) "LOADING OFFICIAL RELEASES…" else "REFRESH OFFICIAL RELEASES")
                        }
                        catalogError?.let { Text("Catalog error: $it", color = Color(0xFFFF8A80), fontSize = 12.sp) }
                        catalog.forEach { item ->
                            Card(
                                onClick = {
                                    version = item.id
                                    prefs.edit().putString("version", item.id).apply()
                                },
                                colors = CardDefaults.cardColors(containerColor = if (version == item.id) Color(0xFF202D49) else panel),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.id, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(item.releaseTime.take(10), color = muted, fontSize = 11.sp)
                                    }
                                    Text(if (version == item.id) "SELECTED" else "SELECT", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (catalog.isEmpty() && !catalogLoading && catalogError == null) {
                            Text("Tap refresh to load the latest 40 stable Minecraft releases from Mojang.", color = muted, fontSize = 12.sp)
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
                                Info("OpenGL ES reported by Android", device.glEs)
                                Info("Live OpenGL version", graphics?.version ?: "Checking GPU…")
                                Info("GPU renderer", graphics?.renderer ?: "Checking GPU…")
                                Info("GPU vendor", graphics?.vendor ?: "Checking GPU…")
                                graphics?.error?.let { Info("Probe status", it) }
                            }
                        }
                        Text("The live EGL probe creates a tiny off-screen graphics context. It does not launch Minecraft or prove renderer compatibility, but it identifies the driver actually exposed to Android apps.", color = muted, fontSize = 12.sp)
                    }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().background(panel), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Home", "Instances", "Studio", "Settings").forEach { name ->
                    TextButton(onClick = { tab = name }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(when (name) {
                                "Home" -> Icons.Default.Gamepad
                                "Instances" -> Icons.Default.Memory
                                "Studio" -> Icons.Default.Gamepad
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
