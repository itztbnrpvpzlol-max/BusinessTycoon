package com.effectcraft.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val bg = Color(0xFF202020)
private val panel = Color(0xFF292929)
private val header = Color(0xFF242424)
private val border = Color(0xFF454545)
private val fg = Color(0xFFE3E3E3)
private val muted = Color(0xFFA5A5A5)
private val purple = Color(0xFFB5A0FF)
private val active = Color(0xFF51456F)

private val panels = listOf("Project", "Effect Controls", "Composition", "Layer", "Timeline", "Info", "Audio", "Preview", "Effects & Presets", "Properties", "Character", "Paragraph", "Align", "Tracker", "Wiggler", "Smoother", "Motion Sketch", "Paint", "Brushes", "Render Queue", "Flowchart", "History", "Markers", "Mask Interpolation", "Script Console", "Essential Graphics", "Lumetri Scopes", "Footage", "Media Browser", "Metadata", "Progress", "Content-Aware Fill", "Create Nulls From Paths", "VR Comp Editor")
private val tools = listOf("Selection", "Hand", "Zoom", "Orbit", "Pan Camera", "Dolly", "Rotate", "Pan Behind", "Rectangle", "Rounded Rectangle", "Ellipse", "Polygon", "Star", "Pen", "Add Vertex", "Delete Vertex", "Convert Vertex", "Mask Feather", "Type", "Vertical Type", "Brush", "Clone Stamp", "Eraser", "Roto Brush", "Refine Edge", "Object Matte", "Puppet")

private data class UiLayer(val id: Int, val name: String, val kind: String, val tint: Color)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(32, 32, 32)
        window.navigationBarColor = android.graphics.Color.rgb(32, 32, 32)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = purple, background = bg, surface = panel, onBackground = fg, onSurface = fg)) {
                EffectCraftWorkspace()
            }
        }
    }
}

@Composable
private fun EffectCraftWorkspace() {
    var panelName by remember { mutableStateOf("Composition") }
    var selectedTool by remember { mutableStateOf("Selection") }
    var selectedLayer by remember { mutableIntStateOf(1) }
    var menu by remember { mutableStateOf<String?>(null) }
    var playhead by remember { mutableFloatStateOf(0f) }
    var query by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf(4) }
    val layers = remember { mutableStateListOf(
        UiLayer(1, "Text Layer", "Text", Color(0xFF9D80CF)),
        UiLayer(2, "Shape Layer", "Shape", Color(0xFF6B9FBF)),
        UiLayer(3, "Background", "Solid", Color(0xFF9B9B65))
    ) }
    val current = layers.firstOrNull { it.id == selectedLayer }

    Column(Modifier.fillMaxSize().background(bg)) {
        Row(Modifier.fillMaxWidth().height(42.dp).background(Color(0xFF191919)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Ef", color = purple, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text("EffectCraft", color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Untitled Project", color = muted, fontSize = 9.sp)
            }
            Text("⋮", color = fg, fontSize = 22.sp)
        }
        Row(Modifier.fillMaxWidth().background(header).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            listOf("File", "Edit", "Composition", "Layer", "Effect", "Animation", "View", "Window", "Help").forEach { name ->
                Box {
                    Text(name, color = if (menu == name) purple else fg, fontSize = 10.sp,
                        modifier = Modifier.clickable { menu = if (menu == name) null else name }.padding(horizontal = 8.dp, vertical = 9.dp))
                    DropdownMenu(expanded = menu == name, onDismissRequest = { menu = null }) {
                        val entries = when (name) {
                            "File" -> listOf("New Project", "Open Project…", "Save", "Save As…", "Import", "Export")
                            "Edit" -> listOf("Undo", "Redo", "Cut", "Copy", "Paste", "Duplicate", "Delete", "Preferences")
                            "Composition" -> listOf("New Composition", "Composition Settings…", "Trim Comp to Work Area")
                            "Layer" -> listOf("New Text Layer", "New Solid", "New Camera", "New Light", "Pre-compose")
                            "Effect" -> listOf("Effect Controls", "Effects & Presets")
                            "Animation" -> listOf("Add Keyframe", "Toggle Animation", "Keyframe Assistant", "Graph Editor")
                            "View" -> listOf("Fit", "50%", "100%", "200%", "Show Grid", "Show Rulers")
                            "Window" -> panels
                            else -> listOf("About EffectCraft", "Documentation", "Keyboard Shortcuts")
                        }
                        entries.forEach { item ->
                            DropdownMenuItem(text = { Text(item, fontSize = 12.sp) }, onClick = {
                                if (item == "New Text Layer" || item == "New Solid") {
                                    val id = nextId++
                                    layers.add(0, UiLayer(id, if (item == "New Solid") "Solid " + id else "Text Layer " + id,
                                        if (item == "New Solid") "Solid" else "Text",
                                        if (item == "New Solid") Color(0xFF9B9B65) else Color(0xFF9D80CF)))
                                    selectedLayer = id
                                }
                                if (panels.contains(item)) panelName = item
                                menu = null
                            })
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(40.dp).background(panel).horizontalScroll(rememberScrollState()).padding(horizontal = 5.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            tools.forEach { tool ->
                val glyph = when (tool) {
                    "Selection" -> "↖"; "Hand" -> "✋"; "Zoom" -> "⌕"; "Rectangle" -> "□"
                    "Ellipse" -> "○"; "Pen" -> "✒"; "Type" -> "T"; "Brush" -> "✎"
                    else -> tool.take(1)
                }
                Box(Modifier.padding(end = 3.dp).size(32.dp).background(if (selectedTool == tool) active else Color.Transparent, RoundedCornerShape(3.dp))
                    .clickable { selectedTool = tool }, contentAlignment = Alignment.Center) {
                    Text(glyph, color = if (selectedTool == tool) Color.White else muted, fontSize = 14.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF1D1D1D)).horizontalScroll(rememberScrollState())) {
            listOf("Project", "Effect Controls", "Composition", "Timeline", "Effects & Presets", "Properties").forEach { tab ->
                Column(Modifier.clickable { panelName = tab }.padding(horizontal = 10.dp, vertical = 7.dp)) {
                    Text(tab, color = if (panelName == tab) fg else muted, fontSize = 10.sp,
                        fontWeight = if (panelName == tab) FontWeight.Bold else FontWeight.Normal)
                    Spacer(Modifier.height(3.dp))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (panelName == tab) purple else Color.Transparent))
                }
            }
        }
        when (panelName) {
            "Composition", "Layer" -> CompositionPanel(layers, selectedLayer, playhead, { playhead = it }, { selectedLayer = it })
            "Timeline" -> TimelinePanel(layers, selectedLayer, playhead, { playhead = it }, { selectedLayer = it }, onAdd = {
                val id = nextId++
                layers.add(0, UiLayer(id, "Text Layer " + id, "Text", Color(0xFF9D80CF)))
                selectedLayer = id
            }, onDelete = { id -> layers.removeAll { it.id == id } })
            "Project" -> ProjectPanel(layers, { selectedLayer = it }, {
                val id = nextId++
                layers.add(0, UiLayer(id, "Footage " + id, "Footage", Color(0xFF729B83)))
            })
            "Effects & Presets" -> EffectsPanel(query, { query = it })
            "Effect Controls" -> EffectControlsPanel(current?.name)
            "Properties" -> PropertiesPanel(current?.name)
            else -> GenericPanel(panelName)
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF1C1C1C)).horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
            listOf("Preview", "Properties", "Align", "Audio", "Effects & Presets", "Character", "Paragraph", "Render Queue").forEach { tab ->
                Text(tab, color = if (panelName == tab) purple else muted, fontSize = 9.sp,
                    modifier = Modifier.clickable { panelName = tab }.padding(horizontal = 9.dp, vertical = 5.dp))
            }
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF191919)).padding(horizontal = 9.dp, vertical = 5.dp)) {
            Text("Tool: " + selectedTool, color = muted, fontSize = 8.sp)
            Spacer(Modifier.weight(1f))
            Text(layers.size.toString() + " layers   •   EffectCraft Mobile", color = muted, fontSize = 8.sp)
        }
    }
}

@Composable
private fun CompositionPanel(layers: List<UiLayer>, selected: Int, time: Float, onTime: (Float) -> Unit, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(bg)) {
        PanelHeader("Composition", "960 × 540   •   30 fps")
        Row(Modifier.fillMaxWidth().height(29.dp).background(panel).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Active Camera", color = muted, fontSize = 9.sp)
            Spacer(Modifier.weight(1f))
            Text("1 View     Fit", color = muted, fontSize = 9.sp)
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF171717)), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth().padding(14.dp).aspectRatio(16f / 9f).background(Color(0xFF101010)).border(1.dp, Color(0xFF666666))) {
                Canvas(Modifier.fillMaxSize()) {
                    val cell = 18.dp.toPx()
                    var y = 0f
                    while (y < size.height) {
                        var x = 0f
                        while (x < size.width) {
                            if (((x / cell).toInt() + (y / cell).toInt()) % 2 == 0)
                                drawRect(Color(0xFF171717), Offset(x, y), Size(cell, cell))
                            x += cell
                        }
                        y += cell
                    }
                    drawRect(Color(0xFF777777), Offset.Zero, size, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
                    drawLine(Color(0x335C9BFF), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
                    drawLine(Color(0x335C9BFF), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx())
                }
                val layer = layers.firstOrNull { it.id == selected }
                if (layer != null) Box(Modifier.align(Alignment.Center).fillMaxWidth(0.48f).height(38.dp).border(1.dp, purple), contentAlignment = Alignment.Center) {
                    Text(layer.name, color = Color.White, fontSize = 11.sp)
                }
                Text("Active Composition", color = muted, fontSize = 8.sp, modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
            }
        }
        Row(Modifier.fillMaxWidth().background(panel).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("◀    ▶    ■", color = purple, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text("00:00:" + (time * 10).toInt().toString().padStart(2, '0') + ":00", color = fg, fontSize = 10.sp)
        }
        TimelinePanel(layers, selected, time, onTime, onSelect, onAdd = {}, onDelete = {})
    }
}

@Composable
private fun TimelinePanel(
    layers: List<UiLayer>, selected: Int, time: Float, onTime: (Float) -> Unit, onSelect: (Int) -> Unit,
    onAdd: () -> Unit, onDelete: (Int) -> Unit
) {
    Column(Modifier.fillMaxWidth().heightIn(min = 210.dp, max = 310.dp).background(panel)) {
        Row(Modifier.fillMaxWidth().height(34.dp).background(header).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Timeline", color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("00:00:00:00", color = muted, fontSize = 9.sp)
            Text("  +  ", color = purple, fontSize = 17.sp, modifier = Modifier.clickable { onAdd() })
            Text("⋮", color = muted, fontSize = 16.sp)
        }
        Row(Modifier.fillMaxWidth().height(25.dp).background(Color(0xFF303030)).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Layer Name", color = muted, fontSize = 8.sp, modifier = Modifier.weight(0.42f))
            Box(Modifier.weight(0.58f).fillMaxHeight().clickable { onTime((time + 0.1f).coerceAtMost(1f) }) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    (0..5).forEach { Text((it * 2).toString() + "s", color = muted, fontSize = 8.sp) }
                }
            }
        }
        if (layers.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Create a layer to begin", color = muted, fontSize = 10.sp)
        } else LazyColumn(Modifier.weight(1f)) {
            items(layers, key = { it.id }) { layer ->
                Row(Modifier.fillMaxWidth().height(34.dp).background(if (selected == layer.id) Color(0xFF3D374A) else panel)
                    .border(0.35.dp, border).clickable { onSelect(layer.id) }, verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(0.42f).fillMaxHeight().padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("◉", color = layer.tint, fontSize = 10.sp)
                        Spacer(Modifier.width(5.dp))
                        Text(layer.name, color = fg, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.weight(0.58f).fillMaxHeight().padding(vertical = 8.dp, horizontal = 3.dp)) {
                        Box(Modifier.fillMaxSize().background(layer.tint.copy(alpha = 0.45f), RoundedCornerShape(2.dp)))
                        Box(Modifier.fillMaxHeight().width(1.dp).background(purple).align(Alignment.CenterStart)
                            .offset(x = (time * 130).dp))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(header).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Current Time Indicator", color = muted, fontSize = 8.sp)
            Spacer(Modifier.weight(1f))
            Text("−", color = muted, modifier = Modifier.clickable { onTime((time - 0.05f).coerceAtLeast(0f)) })
            Text("  ◆  ", color = purple, modifier = Modifier.clickable { })
            Text("+", color = muted, modifier = Modifier.clickable { onTime((time + 0.05f).coerceAtMost(1f)) })
        }
    }
}

@Composable
private fun ProjectPanel(layers: List<UiLayer>, onSelect: (Int) -> Unit, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().background(panel)) {
        PanelHeader("Project", "Name   •   Label")
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Search Project", color = muted, fontSize = 10.sp, modifier = Modifier.weight(1f).background(header, RoundedCornerShape(3.dp)).padding(10.dp))
            Text("＋", color = purple, fontSize = 22.sp, modifier = Modifier.clickable { onAdd() }.padding(horizontal = 10.dp))
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF303030)).padding(8.dp)) {
            Text("Name", color = muted, fontSize = 9.sp, modifier = Modifier.weight(1f))
            Text("Label", color = muted, fontSize = 9.sp)
        }
        layers.forEach { layer ->
            Row(Modifier.fillMaxWidth().clickable { onSelect(layer.id) }.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (layer.kind == "Text") "T" else if (layer.kind == "Shape") "◇" else "▣", color = layer.tint, fontSize = 14.sp)
                Spacer(Modifier.width(9.dp))
                Text(layer.name, color = fg, fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(9.dp).background(layer.tint, RoundedCornerShape(2.dp)))
            }
            HorizontalDivider(color = border, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun EffectsPanel(query: String, onQuery: (String) -> Unit) {
    val categories = listOf("Animation Presets", "3D Channel", "Audio", "Blur & Sharpen", "Channel", "Color Correction", "Distort", "Generate", "Keying", "Matte", "Noise & Grain", "Perspective", "Simulation", "Stylize", "Text", "Time", "Transition", "Utility")
    Column(Modifier.fillMaxSize().background(panel)) {
        PanelHeader("Effects & Presets", "Browse effects")
        OutlinedTextField(query, onQuery, modifier = Modifier.fillMaxWidth().padding(8.dp), placeholder = { Text("Search effects & presets", fontSize = 10.sp) }, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            categories.filter { query.isBlank() || it.contains(query, true) }.forEach { category ->
                Text("▸  " + category, color = fg, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 12.dp, vertical = 10.dp))
                HorizontalDivider(color = border, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun EffectControlsPanel(layer: String?) {
    Column(Modifier.fillMaxSize().background(panel)) {
        PanelHeader("Effect Controls", layer ?: "No layer selected")
        if (layer == null) EmptyPanel("Select a layer to inspect its effects.")
        else {
            Text(layer, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(12.dp))
            Text("Effects", color = muted, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 12.dp))
            EmptyPanel("No effects applied")
        }
    }
}

@Composable
private fun PropertiesPanel(layer: String?) {
    Column(Modifier.fillMaxSize().background(panel)) {
        PanelHeader("Properties", layer ?: "No Selection")
        if (layer == null) EmptyPanel("Select a layer to display its properties.")
        else {
            Text(layer, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(12.dp))
            listOf("Anchor Point" to "480, 270", "Position" to "480, 270", "Scale" to "100%, 100%", "Rotation" to "0°", "Opacity" to "100%").forEach { pair ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(pair.first, color = muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text(pair.second, color = fg, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun GenericPanel(name: String) {
    Column(Modifier.fillMaxSize().background(panel)) {
        PanelHeader(name, "EffectCraft panel")
        EmptyPanel(name + " panel")
    }
}

@Composable
private fun PanelHeader(title: String, detail: String) {
    Row(Modifier.fillMaxWidth().height(33.dp).background(header).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(detail, color = muted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("  ⋮", color = muted, fontSize = 14.sp)
    }
    HorizontalDivider(color = border, thickness = 0.5.dp)
}

@Composable
private fun EmptyPanel(message: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = muted, fontSize = 10.sp)
    }
}
