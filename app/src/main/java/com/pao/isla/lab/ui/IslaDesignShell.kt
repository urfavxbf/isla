package com.pao.isla.lab.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pao.isla.lab.R

/**
 * The Isla dashboard and navigation shell. This layer intentionally does not own any
 * service, permission or preference state; the existing screen passes it real values.
 */
internal object IslaUiTokens {
    val shape = RoundedCornerShape(24.dp)
    val smallShape = RoundedCornerShape(18.dp)
    val violet = Color(0xFF9C7AFF)
    val charcoal = Color(0xFF15141F)
}

@Composable
internal fun IslaPageIntro(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun IslaHubHero(
    enabled: Boolean,
    hasPermissions: Boolean,
    onPermissionClick: () -> Unit
) {
    val status = when {
        !hasPermissions -> "Setup needed"
        enabled -> "Running"
        else -> "Ready to start"
    }
    Card(
        shape = IslaUiTokens.shape,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IslaUiTokens.charcoal),
        border = BorderStroke(1.dp, IslaUiTokens.violet.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF242039), IslaUiTokens.charcoal, Color(0xFF191624))
                    )
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFBDAFFF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "YOUR ISLAND, YOUR WAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp,
                    color = Color(0xFFCFC5F8)
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black,
                    border = BorderStroke(1.dp, IslaUiTokens.violet.copy(alpha = 0.55f)),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(7.dp).clip(RoundedCornerShape(50))
                                .background(if (enabled && hasPermissions) Color(0xFF55D6A7) else IslaUiTokens.violet)
                        )
                        Text(
                            "isla",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 1.1.sp
                        )
                        Box(
                            Modifier.size(7.dp).clip(RoundedCornerShape(50))
                                .background(IslaUiTokens.violet)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Island status",
                        color = Color(0xFFB8B3CE),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        status,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!hasPermissions) {
                    Surface(
                        color = Color(0xFF3A304F),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable(role = Role.Button, onClick = onPermissionClick)
                    ) {
                        Text(
                            "Set up",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = status,
                        tint = if (enabled) Color(0xFF55D6A7) else Color(0xFFC6B8FF)
                    )
                }
            }
        }
    }
}

@Composable
internal fun IslaSectionJump(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(IslaUiTokens.smallShape)
            .clickable(role = Role.Button, onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = IslaUiTokens.smallShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            Modifier.padding(horizontal = 15.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(13.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(11.dp).size(22.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun IslaHomeShortcuts(
    onTabClick: (StudioTab) -> Unit,
    onNotificationsClick: () -> Unit
) {
    Text(
        "Explore Isla",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    IslaSectionJump("Overlay studio", "Shape, cutout position and animations", Icons.Rounded.Tune) {
        onTabClick(StudioTab.Position)
    }
    IslaSectionJump("Live features", "Music, notifications, calls and charging", Icons.Rounded.NotificationsActive) {
        onTabClick(StudioTab.Features)
    }
    IslaSectionJump("Appearance", "Colors, glass, typography and effects", Icons.Rounded.Palette) {
        onTabClick(StudioTab.Style)
    }
    IslaSectionJump("Notification privacy", "Choose which apps can appear on the island", Icons.Rounded.Security) {
        onNotificationsClick()
    }
}

@Composable
internal fun IslaBottomNav(
    selectedTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit
) {
    val destinations = listOf(
        Triple(StudioTab.Studio, R.string.isla_nav_home, Icons.Rounded.Dashboard),
        Triple(StudioTab.Position, R.string.isla_nav_overlay, Icons.Rounded.Tune),
        Triple(StudioTab.Features, R.string.isla_nav_features, Icons.Rounded.NotificationsActive),
        Triple(StudioTab.Style, R.string.isla_nav_style, Icons.Rounded.Palette),
        Triple(StudioTab.Settings, R.string.isla_nav_system, Icons.Rounded.Security)
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation = 0.dp) {
        destinations.forEach { (tab, label, icon) ->
            val name = stringResource(label)
            NavigationBarItem(
                selected = selectedTab == tab || (tab == StudioTab.Features && selectedTab == StudioTab.Demo),
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(name, maxLines = 1) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
