package com.pao.isla

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0B12)
private val Panel = Color(0xFF171722)
private val Muted = Color(0xFFAAA8B8)
private val Accent = Color(0xFFB9A2FF)

class MainActivity : ComponentActivity() {
    private var permissionVersion by mutableIntStateOf(0)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionVersion++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
                IslaScreen(permissionVersion, {
                    permissionLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }, {
                    if (Settings.canDrawOverlays(this)) startService(Intent(this, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_START))
                }, {
                    startService(Intent(this, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_STOP))
                })
            }
        }
    }
}

@Composable
private fun IslaScreen(version: Int, grant: () -> Unit, start: () -> Unit, stop: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var c by remember { mutableStateOf(IslandConfig.load(context)) }
    val permitted = remember(version) { Settings.canDrawOverlays(context) }

    fun update(next: IslandConfig) {
        c = next
        next.save(context)
        context.startService(Intent(context, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_UPDATE))
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFF252039), shape = RoundedCornerShape(18.dp)) {
                    Text("◉", modifier = Modifier.padding(13.dp), color = Accent, fontSize = 22.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Isla", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Your phone. Your style.", color = Muted)
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LIVE PREVIEW", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Spacer(Modifier.height(18.dp))
                    Box(Modifier.fillMaxWidth().height(76.dp), contentAlignment = Alignment.TopCenter) {
                        Row(Modifier.width(c.widthDp.dp).height(c.heightDp.dp).alpha(c.transparency)
                            .background(Color.Black, RoundedCornerShape(c.radiusDp.dp)).padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(Modifier.size(8.dp).background(c.accent, CircleShape))
                            Text("Isla", color = Color.White, fontSize = 11.sp)
                            Box(Modifier.size(6.dp).background(Color(0xFF48D597), CircleShape))
                        }
                    }
                    Text("${c.widthDp} × ${c.heightDp} dp", color = Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Customize", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SliderRow("Width", "${c.widthDp} dp", c.widthDp.toFloat(), 140f, 300f) { update(c.copy(widthDp = it.toInt())) }
                    SliderRow("Height", "${c.heightDp} dp", c.heightDp.toFloat(), 28f, 64f) { update(c.copy(heightDp = it.toInt())) }
                    SliderRow("Corner roundness", "${c.radiusDp} dp", c.radiusDp.toFloat(), 12f, 36f) { update(c.copy(radiusDp = it.toInt())) }
                    SliderRow("Transparency", "${(c.transparency * 100).toInt()}%", c.transparency, 0.45f, 1f) { update(c.copy(transparency = it)) }
                    Text("Accent color", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(0xFF8B5CF6, 0xFF38BDF8, 0xFF34D399, 0xFFFB7185, 0xFFFBBF24).forEach { value ->
                            Box(Modifier.size(38.dp).background(Color(value.toInt()), CircleShape).clickable { update(c.copy(accentHex = value)) }, contentAlignment = Alignment.Center) {
                                if (c.accentHex == value) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Floating island", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(if (permitted) "Overlay permission is enabled." else "Allow Isla to display over other apps to use the floating island.", color = Muted)
                    if (!permitted) Button(onClick = grant, modifier = Modifier.fillMaxWidth()) { Text("Grant overlay permission") }
                    else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = start, modifier = Modifier.weight(1f)) { Text("▶"); Spacer(Modifier.width(6.dp)); Text("Start") }
                        OutlinedButton(onClick = stop, modifier = Modifier.weight(1f)) { Text("■"); Spacer(Modifier.width(6.dp)); Text("Stop") }
                    }
                    OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))) }, modifier = Modifier.fillMaxWidth()) { Text("Open overlay settings") }
                }
            }
            Text("Android 8.0+. Media, calls and notification integrations can be added later using their required Android permissions.", color = Color(0xFF888696), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SliderRow(title: String, label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(label, color = Accent, style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = value, onValueChange = onChange, valueRange = min..max)
    }
}
