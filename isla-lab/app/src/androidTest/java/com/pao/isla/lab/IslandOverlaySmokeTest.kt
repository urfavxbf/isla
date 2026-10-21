/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pao.isla.lab

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pao.isla.lab.data.IslaSettings
import com.pao.isla.lab.model.IslandMode
import com.pao.isla.lab.model.IslandNotification
import com.pao.isla.lab.ui.IslandCollapsedContent
import com.pao.isla.lab.ui.IslaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IslandOverlaySmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun collapsed_renders_battery() {
        compose.setContent {
            IslaTheme {
                IslandCollapsedContent(
                    mode = IslandMode.Battery,
                    notification = IslandNotification(
                        key = "t", packageName = "x", appName = "x",
                        title = "Charging", text = "49%", timeMillis = 0,
                        mode = IslandMode.Battery
                    ),
                    collapsedAlpha = 1f,
                    settings = IslaSettings.Default
                )
            }
        }
        compose.onNodeWithText("49%").assertExists()
    }
}
