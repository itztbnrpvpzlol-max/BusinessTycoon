package com.aetherlauncher.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val canvas = Color(0xFF090B10)
private val surface = Color(0xFF151922)
private val raised = Color(0xFF202735)
private val violet = Color(0xFF9B87FF)
private val secondary = Color(0xFF9AA4B5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = violet,
                background = canvas,
                surface = surface,
                onBackground = Color.White,
                onSurface = Color.White
            )) {
                EffectCraftEditor()
            }
        }
    }
}

private data class EditorLayer(val id: Int, val name: String, val kind: String)

@Composable
private fun EffectCraftEditor() {
    var selectedLayer by remember { mutableIntStateOf(1) }
    var playing by remember { mutableStateOf(false) }
    var playhead by remember { mutableFloatStateOf(0.18f) }
    var showProperties by remember { mutableStateOf(true) }
    var nextId by remember { mutableIntStateOf(4) }
    val layers = remember { mutableStateListOf(
        EditorLayer(1, "Text Layer", "TEXT"),
        EditorLayer(2, "Shape Layer", "SHAPE"),
        EditorLayer(3, "Background", "SOLID")
    ) }

    Surface(Modifier.fillMaxSize(), color = canvas) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().background(surface).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = secondary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("EFFECTCRAFT", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Text("Untitled Composition", color = secondary, fontSize = 11.sp)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.Save, "Save project", tint = Color.White) }
                IconButton(onClick = { showProperties = !showProperties }) {
                    Icon(Icons.Default.Tune, "Toggle properties", tint = violet)
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("COMPOSITION VIEWER", color = secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text("960 × 540  •  30 FPS", color = secondary, fontSize = 10.sp)
                }
                Box(
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color(0xFF050608), RoundedCornerShape(10.dp)).border(1.dp, raised, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("EffectCraft", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("MOBILE COMPOSITION PREVIEW", color = violet, fontSize = 9.sp, letterSpacing = 2.sp)
                    }
                    Text("PREVIEW", Modifier.align(Alignment.TopStart).padding(9.dp), color = secondary, fontSize = 9.sp)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("TIMELINE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Text("00:00:05:12", color = secondary, fontSize = 11.sp)
                    IconButton(onClick = { playing = !playing }) {
                        Icon(Icons.Default.PlayArrow, if (playing) "Pause" else "Play", tint = violet)
                    }
                }
                Box(Modifier.fillMaxWidth().height(30.dp).background(surface, RoundedCornerShape(6.dp)).clickable {
                    playhead = (playhead + 0.08f).coerceAtMost(1f)
                }) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        (0..5).forEach { Text("0${it * 2}s", color = secondary, fontSize = 9.sp) }
                    }
                    Box(Modifier.fillMaxHeight().fillMaxWidth(playhead).padding(end = 1.dp), contentAlignment = Alignment.CenterEnd) {
                        Box(Modifier.width(2.dp).fillMaxHeight().background(violet))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    layers.asReversed().forEach { layer ->
                        Row(
                            Modifier.fillMaxWidth().height(42.dp).background(if (selectedLayer == layer.id) Color(0xFF28233E) else surface, RoundedCornerShape(6.dp))
                                .clickable { selectedLayer = layer.id }.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(if (layer.kind == "TEXT") Icons.Default.TextFields else Icons.Default.Layers, null, tint = violet, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(9.dp))
                            Text(layer.name, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Box(Modifier.fillMaxWidth(0.43f).height(22.dp).background(if (selectedLayer == layer.id) Color(0xFF6554A8) else Color(0xFF34445C), RoundedCornerShape(4.dp)))
                        }
                    }
                }
                if (showProperties) {
                    Text("LAYER CONTROLS", color = secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Transform", "Opacity", "Effects", "Keyframes", "Blend").forEach {
                            AssistChip(onClick = { }, label = { Text(it, fontSize = 11.sp) })
                        }
                    }
                    Text("Select a layer to inspect its properties.", color = secondary, fontSize = 11.sp)
                }
            }
            Row(Modifier.fillMaxWidth().background(surface).horizontalScroll(rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditorTool("Add layer", Icons.Default.Add) {
                    layers.add(EditorLayer(nextId++, "New Layer", "SHAPE"))
                }
                EditorTool("Text", Icons.Default.TextFields) {
                    layers.add(EditorLayer(nextId++, "Text Layer", "TEXT"))
                }
                EditorTool("Trim", Icons.Default.ContentCut) { }
                EditorTool("Effects", Icons.Default.Tune) { showProperties = true }
                EditorTool("Settings", Icons.Default.Settings) { }
            }
            Text(
                "MOBILE PORT FOUNDATION • RENDER ENGINE NOT CONNECTED YET",
                Modifier.fillMaxWidth().background(canvas).padding(vertical = 6.dp),
                color = secondary,
                fontSize = 8.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun EditorTool(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, colors = ButtonDefaults.filledTonalButtonColors(containerColor = raised)) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp)
    }
}
