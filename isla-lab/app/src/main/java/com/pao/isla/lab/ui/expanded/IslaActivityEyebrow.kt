package com.pao.isla.lab.ui.expanded

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Isla's common expanded-card identity. Always uses the activity's configured accent,
 * keeps the existing palette's text contrast, and does not own any touch or action event.
 * As a simple display-only component it cannot override incoming call, media or reply controls.
 */
@Composable
internal fun IslaActivityEyebrow(
    label: String,
    accent: Color,
    palette: IslandPalette = LocalIslandPalette.current,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(palette.tint(0.07f))
            .heightIn(min = 18.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = label.uppercase(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = palette.textSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            lineHeight = 11.sp
        )
    }
}
