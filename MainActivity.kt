package com.example.glyphstudio

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF171923)
private val Paper = Color(0xFFF7F7FB)
private val Purple = Color(0xFF7357FF)
private data class StrokeData(val points: List<Offset>, val color: Color, val width: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Purple, background = Paper, surface = Color.White)) { GlyphStudioApp() } }
    }
}

@Composable
private fun GlyphStudioApp() {
    val context = LocalContext.current
    val alphabet = remember { (('A'..'Z').map { it.toString() } + ('a'..'z').map { it.toString() } + ('0'..'9').map { it.toString() } + listOf("!", "?", ".", ",", "@", "&")).toList() }
    var selected by remember { mutableIntStateOf(0) }
    var inkColor by remember { mutableStateOf(Ink) }
    var brushSize by remember { mutableFloatStateOf(8f) }
    var strokesByGlyph by remember { mutableStateOf(mapOf<String, List<StrokeData>>()) }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasWidth by remember { mutableFloatStateOf(1f) }
    var canvasHeight by remember { mutableFloatStateOf(1f) }
    var showExport by remember { mutableStateOf(false) }
    val glyph = alphabet[selected]
    val strokes = strokesByGlyph[glyph].orEmpty()

    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("GLYPH STUDIO", fontSize = 12.sp, color = Purple, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Make your own font", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            }
            Text("${selected + 1}/${alphabet.size}", color = Color.DarkGray, fontSize = 13.sp)
        }
        Text("CHARACTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.4.sp)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            alphabet.forEachIndexed { index, char ->
                Surface(onClick = { selected = index; currentPoints = emptyList() }, shape = RoundedCornerShape(12.dp), color = if (selected == index) Purple else Color.White, modifier = Modifier.size(43.dp).border(1.dp, if (selected == index) Purple else Color(0xFFE6E5ED), RoundedCornerShape(12.dp))) {
                    Box(contentAlignment = Alignment.Center) { Text(char, color = if (selected == index) Color.White else Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("DRAW GLYPH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.4.sp, modifier = Modifier.weight(1f))
            Text("${strokes.size} strokes", fontSize = 12.sp, color = Color.Gray)
        }
        Box(Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp, bottom = 12.dp).background(Color.White, RoundedCornerShape(22.dp)).border(1.dp, Color(0xFFE7E6EF), RoundedCornerShape(22.dp))) {
            Canvas(Modifier.fillMaxSize().padding(8.dp).pointerInput(glyph, inkColor, brushSize, strokesByGlyph) {
                canvasWidth = size.width.toFloat().coerceAtLeast(1f)
                canvasHeight = size.height.toFloat().coerceAtLeast(1f)
                detectDragGestures(onDragStart = { start -> currentPoints = listOf(start) }, onDrag = { change, _ -> currentPoints = currentPoints + change.position }, onDragEnd = {
                    if (currentPoints.size > 1) strokesByGlyph = strokesByGlyph + (glyph to (strokes + StrokeData(currentPoints, inkColor, brushSize)))
                    currentPoints = emptyList()
                })
            }) {
                val left = size.width * .12f; val right = size.width * .88f
                val top = size.height * .15f; val baseline = size.height * .82f; val mid = size.height * .49f
                val grid = Color(0xFFE9E8F0)
                drawLine(grid, Offset(left, top), Offset(right, top), 1.dp.toPx())
                drawLine(grid, Offset(left, mid), Offset(right, mid), 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                drawLine(grid, Offset(left, baseline), Offset(right, baseline), 1.dp.toPx())
                drawLine(grid, Offset(left, top), Offset(left, baseline), 1.dp.toPx())
                drawLine(grid, Offset(right, top), Offset(right, baseline), 1.dp.toPx())
                (strokes + if (currentPoints.size > 1) listOf(StrokeData(currentPoints, inkColor, brushSize)) else emptyList()).forEach { stroke ->
                    val path = Path().apply { stroke.points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } }
                    drawPath(path, stroke.color, style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                }
            }
            Text("Draw with your finger", Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), color = Color.LightGray, fontSize = 12.sp)
        }
        Text("BRUSH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.4.sp)
        Row(Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(Ink, Purple, Color(0xFFE84D8A), Color(0xFF168A70), Color(0xFFEF9A24)).forEach { color ->
                Surface(onClick = { inkColor = color }, shape = CircleShape, color = color, modifier = Modifier.size(31.dp).then(if (inkColor == color) Modifier.border(3.dp, Color(0xFFB9B1FF), CircleShape) else Modifier)) {}
            }
            Spacer(Modifier.weight(1f))
            Text("${brushSize.toInt()} px", fontSize = 12.sp, color = Color.Gray)
        }
        Slider(value = brushSize, onValueChange = { brushSize = it }, valueRange = 2f..20f, modifier = Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { strokesByGlyph = strokesByGlyph + (glyph to strokes.dropLast(1)); currentPoints = emptyList() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), enabled = strokes.isNotEmpty()) { Text("Undo") }
            OutlinedButton(onClick = { strokesByGlyph = strokesByGlyph - glyph; currentPoints = emptyList() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Clear") }
            Button(onClick = { showExport = true }, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(14.dp)) { Text("Export") }
        }
    }
    if (showExport) AlertDialog(onDismissRequest = { showExport = false }, title = { Text("Export your glyph") }, text = { Text("Save the current character as a PNG image. Full font-file generation (TTF/OTF) is planned for a later build stage.") }, confirmButton = { TextButton(onClick = {
        val bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap); canvas.drawColor(android.graphics.Color.WHITE)
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.LTGRAY; strokeWidth = 2f; style = AndroidPaint.Style.STROKE }
        canvas.drawLine(120f, 160f, 904f, 160f, paint); canvas.drawLine(120f, 840f, 904f, 840f, paint)
        strokes.forEach { stroke ->
            val p = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb((stroke.color.red*255).toInt(), (stroke.color.green*255).toInt(), (stroke.color.blue*255).toInt()); strokeWidth = stroke.width * 2.5f; style = AndroidPaint.Style.STROKE; strokeCap = AndroidPaint.Cap.ROUND; strokeJoin = AndroidPaint.Join.ROUND }
            val path = AndroidPath(); stroke.points.forEachIndexed { i, point -> val x = point.x / canvasWidth * 1024f; val y = point.y / canvasHeight * 1024f; if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }; canvas.drawPath(path, p)
        }
        val filename = "glyph_${glyph}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.png"
        val values = ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, filename); put(MediaStore.Images.Media.MIME_TYPE, "image/png"); if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GlyphStudio") }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val saved = uri?.let { context.contentResolver.openOutputStream(it)?.use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) } } ?: false
        Toast.makeText(context, if (saved) "PNG saved to Pictures/GlyphStudio" else "Could not save PNG", Toast.LENGTH_LONG).show(); showExport = false
    }) { Text("Save PNG") }, dismissButton = { TextButton(onClick = { showExport = false }) { Text("Cancel") } })
}
