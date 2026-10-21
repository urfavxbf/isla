package com.pao.isla.lab.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pao.isla.lab.data.IslaSettings
import com.pao.isla.lab.data.IslaSettingsRepository
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Preview-first controls for the Isla Overlay tab. Does not draw the actual
 * WindowManager overlay or change accessibility/media service behavior.
 */
@Composable
internal fun IslaOverlayStudio(
    settings: IslaSettings,
    repository: IslaSettingsRepository,
    landscape: Boolean
) {
    val config = LocalConfiguration.current
    val deviceWidth = if (landscape) {
        maxOf(config.screenWidthDp, config.screenHeightDp).toFloat()
    } else {
        minOf(config.screenWidthDp, config.screenHeightDp).toFloat()
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IslaOverlayPreview(settings, landscape, deviceWidth)
        Text(
            text = if (landscape) "LANDSCAPE ANCHOR" else "ONE-TAP LAYOUTS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (landscape) {
            IslaLandscapeAnchors(settings, repository)
        } else {
            IslaPortraitPresets(settings, repository, deviceWidth)
        }
        Text(
            "The preview is a visual guide. The real island follows your phone's cutout and Android display insets. Fine-tune all controls in Advanced layout.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IslaOverlayPreview(
    settings: IslaSettings,
    landscape: Boolean,
    deviceWidthDp: Float
) {
    val isNotch = !landscape && settings.enableNotchMode
    val previewWidth = if (landscape) settings.landscapeWidth else settings.width
    val previewHeight = if (landscape) settings.landscapeHeight else settings.height
    val xOffset = if (landscape) settings.landscapeXOffset else settings.xOffset
    val yOffset = if (landscape) settings.landscapeYOffset else settings.yOffset
    val anchor = if (landscape) settings.landscapePillPosition else "middle"
    val orientation = if (landscape) "Landscape" else "Portrait"
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF171523),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.38f))
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "ISLA LIVE STYLE",
                    color = Color(0xFFC9BAFF),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(orientation, style = MaterialTheme.typography.labelMedium, color = Color(0xFFC9C4D5))
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (landscape) 158.dp else 210.dp)
                    .semantics {
                        contentDescription =
                            "Illustrative " + orientation + " island position, " +
                            previewWidth.toInt() + " by " + previewHeight.toInt() + " dp"
                    }
            ) {
                val screenWidth = if (landscape) size.width * .93f else size.width * .64f
                val screenHeight = if (landscape) size.height * .83f else size.height * .98f
                val screenLeft = (size.width - screenWidth) / 2f
                val screenTop = (size.height - screenHeight) / 2f
                val outerRound = if (landscape) 35f else 44f
                drawRoundRect(
                    color = Color(0xFF353146),
                    topLeft = Offset(screenLeft - 2f, screenTop - 2f),
                    size = Size(screenWidth + 4f, screenHeight + 4f),
                    cornerRadius = CornerRadius(outerRound),
                    style = Stroke(width = 2.5f)
                )
                drawRoundRect(
                    color = Color(0xFF090B14),
                    topLeft = Offset(screenLeft, screenTop),
                    size = Size(screenWidth, screenHeight),
                    cornerRadius = CornerRadius(outerRound)
                )
                // A muted wallpaper wash helps reveal the black island at the top.
                drawRoundRect(
                    color = Color(0xFF18132A),
                    topLeft = Offset(screenLeft + 6f, screenTop + screenHeight * .30f),
                    size = Size(screenWidth - 12f, screenHeight * .47f),
                    cornerRadius = CornerRadius(22f)
                )
                val insetTop = if (isNotch) 0f else screenHeight * .035f
                val pill = islaPreviewPill(
                    frameWidthPx = screenWidth,
                    frameHeightPx = screenHeight - insetTop,
                    screenWidthDp = deviceWidthDp,
                    pillWidthDp = previewWidth,
                    pillHeightDp = previewHeight,
                    xOffsetDp = xOffset,
                    yOffsetDp = yOffset,
                    anchor = anchor,
                    docked = isNotch
                )
                drawRoundRect(
                    color = primary.copy(alpha = .32f),
                    topLeft = Offset(screenLeft + pill.left - 3f, screenTop + insetTop + pill.top - 3f),
                    size = Size(pill.width + 6f, pill.height + 6f),
                    cornerRadius = CornerRadius(if (isNotch) 12f else pill.height / 2f + 4f)
                )
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(screenLeft + pill.left, screenTop + insetTop + pill.top),
                    size = Size(pill.width, pill.height),
                    cornerRadius = CornerRadius(if (isNotch) 9f else pill.height / 2f)
                )
                if (pill.width > 25f && pill.height > 8f) {
                    drawCircle(
                        color = Color(0xFFBBA1FF),
                        radius = (pill.height * .18f).coerceIn(2f, 6f),
                        center = Offset(screenLeft + pill.left + pill.width * .20f,
                            screenTop + insetTop + pill.top + pill.height / 2f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = .72f),
                        topLeft = Offset(screenLeft + pill.left + pill.width * .42f,
                            screenTop + insetTop + pill.top + pill.height * .43f),
                        size = Size(pill.width * .35f, (pill.height * .12f).coerceAtLeast(1f)),
                        cornerRadius = CornerRadius(4f)
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Width " + previewWidth.toInt() + " dp", color = Color(0xFFC9C4D5),
                    style = MaterialTheme.typography.labelSmall)
                Text("Height " + previewHeight.toInt() + " dp", color = Color(0xFFC9C4D5),
                    style = MaterialTheme.typography.labelSmall)
                Text(if (isNotch) "Docked" else "Offset " + xOffset.toInt() + " dp",
                    color = Color(0xFFC9C4D5), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun IslaPortraitPresets(
    settings: IslaSettings,
    repository: IslaSettingsRepository,
    screenWidthDp: Float
) {
    val scope = rememberCoroutineScope()
    val horizontalEdge = (screenWidthDp / 2f - 40f)
        .coerceIn(30f, IslaSettings.MAX_X_OFFSET)
    val choices = listOf(
        Triple("Center", "center", abs(settings.xOffset) < 5f &&
            abs(settings.width - 112f) < 8f && !settings.enableNotchMode),
        Triple("Wide", "wide", abs(settings.xOffset) < 5f &&
            abs(settings.width - 150f) < 8f && !settings.enableNotchMode),
        Triple("Left", "left", settings.xOffset < -30f && !settings.enableNotchMode),
        Triple("Right", "right", settings.xOffset > 30f && !settings.enableNotchMode),
        Triple("Notch", "notch", settings.enableNotchMode)
    )
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        choices.chunked(3).forEach { group ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                group.forEach { (label, id, selected) ->
                    IslaLayoutChoice(label, selected, Modifier.weight(1f)) {
                        scope.launch {
                            val geometry = when (id) {
                                "wide" -> listOf(150f, 38f, 0f, 12f, 22f)
                                "left" -> listOf(105f, 34f, -horizontalEdge, 10f, 20f)
                                "right" -> listOf(105f, 34f, horizontalEdge, 10f, 20f)
                                "notch" -> listOf(175f, 35f, 0f, 0f, 20f)
                                else -> listOf(112f, 34f, 0f, 10f, 20f)
                            }
                            repository.setEnableNotchMode(id == "notch")
                            repository.setPosition(
                                width = geometry[0],
                                height = geometry[1],
                                xOffset = geometry[2],
                                yOffset = geometry[3]
                            )
                            repository.setCornerRadius(geometry[4])
                        }
                    }
                }
                repeat(3 - group.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun IslaLandscapeAnchors(settings: IslaSettings, repository: IslaSettingsRepository) {
    val scope = rememberCoroutineScope()
    val options = listOf(
        "Camera" to IslaSettings.LANDSCAPE_POSITION_AUTO,
        "Left" to IslaSettings.LANDSCAPE_POSITION_LEFT,
        "Center" to IslaSettings.LANDSCAPE_POSITION_MIDDLE,
        "Right" to IslaSettings.LANDSCAPE_POSITION_RIGHT
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, id) ->
            IslaLayoutChoice(label, settings.landscapePillPosition == id, Modifier.weight(1f)) {
                scope.launch { repository.setLandscapePillPosition(id) }
            }
        }
    }
}

@Composable
private fun IslaLayoutChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 14.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
