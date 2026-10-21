package com.pao.isla

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.app.NotificationManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextAlign
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

    override fun onResume() {
        super.onResume()
        // Re-check permissions after returning from Android Settings.
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
    val musicAccess = remember(version) { hasMusicAccess(context) }

    fun update(next: IslandConfig) {
        c = next
        next.save(context)
        if (Settings.canDrawOverlays(context)) {
            context.startService(Intent(context, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_UPDATE))
        }
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
                    Box(Modifier.fillMaxWidth().height(78.dp), contentAlignment = Alignment.TopCenter) {
                        Box(
                            Modifier.width(c.widthDp.dp).height(c.heightDp.dp)
                                .alpha(c.transparency)
                                .background(Color.Black, RoundedCornerShape(c.radiusDp.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!c.isCompact) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(Modifier.size(7.dp).background(c.accent, CircleShape))
                                    Text("Isla", color = Color.White, fontSize = 11.sp)
                                    Box(Modifier.size(6.dp).background(Color(0xFF48D597), CircleShape))
                                }
                            } else if (!c.isHoleSize) {
                                Text("●", color = c.accent, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Text("${c.widthDp} × ${c.heightDp} dp", color = Muted, style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Shape presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { update(c.copy(widthDp = 36, heightDp = 36, radiusDp = 36)) }, label = { Text("●  Hole") })
                        AssistChip(onClick = { update(c.copy(widthDp = 88, heightDp = 32, radiusDp = 28)) }, label = { Text("Compact") })
                        AssistChip(onClick = { update(c.copy(widthDp = 190, heightDp = 36, radiusDp = 24)) }, label = { Text("Standard") })
                        AssistChip(onClick = { update(c.copy(widthDp = 260, heightDp = 42, radiusDp = 30)) }, label = { Text("Wide") })
                    }
                    Text("Use Hole for circular front cameras, then fine-tune below.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text("Customize", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SliderRow("Width", "${c.widthDp} dp", c.widthDp.toFloat(), 28f, 300f) { update(c.copy(widthDp = it.toInt())) }
                    SliderRow("Height", "${c.heightDp} dp", c.heightDp.toFloat(), 24f, 64f) { update(c.copy(heightDp = it.toInt())) }
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
                    Text("Camera cutout alignment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Move the island up or down to align it with the punch-hole. Default is 0 dp (screen top).", color = Muted, style = MaterialTheme.typography.bodySmall)
                    val positionLabel = if (c.positionYDp == 0) "0 dp" else if (c.positionYDp < 0) "${-c.positionYDp} dp up" else "${c.positionYDp} dp down"
                    SliderRow("Vertical position", positionLabel, c.positionYDp.toFloat(), -72f, 80f) { update(c.copy(positionYDp = it.toInt())) }
                    OutlinedButton(onClick = { update(c.copy(positionYDp = 0)) }, modifier = Modifier.fillMaxWidth()) { Text("Reset vertical position") }
                    Text("Android and phone manufacturers may draw the status bar over regular app overlays. Exact camera-hole coverage cannot be guaranteed on every device.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }


            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Now playing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Show live music titles and control playback from the floating island.", color = Muted)
                    Text(
                        if (musicAccess) "Music access enabled" else "Music access is off",
                        color = if (musicAccess) Color(0xFF55D8A4) else Muted,
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (!musicAccess) {
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Enable music access") }
                    } else {
                        OutlinedButton(
                            onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Manage music access") }
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Show music on island", style = MaterialTheme.typography.bodyMedium)
                            Text("Track name and playback controls", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = c.showMedia, onCheckedChange = { update(c.copy(showMedia = it)) })
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Expand while playing", style = MaterialTheme.typography.bodyMedium)
                            Text("Enlarge small camera-hole pill for song titles", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = c.expandOnPlay,
                            enabled = c.showMedia,
                            onCheckedChange = { update(c.copy(expandOnPlay = it)) }
                        )
                    }
                    Text(
                        "Android grants broad notification access for this feature. Isla only reads media-session metadata; it does not save or upload your notifications.",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
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
            Text("Android 8.0+. Calls and non-media notification integrations are not included yet.", color = Color(0xFF888696), style = MaterialTheme.typography.bodySmall)
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

private fun hasMusicAccess(context: Context): Boolean {
    val component = ComponentName(context, IslaMusicListener::class.java)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .isNotificationListenerAccessGranted(component)
    } else {
        Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )?.split(':')?.any { ComponentName.unflattenFromString(it) == component } == true
    }
}
