/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pao.isla.lab.model

import com.pao.isla.lab.data.IslaSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingWindowModeTest {
    @Test
    fun offWinsOverAnyMode() {
        assertEquals(FloatingWindowRoute.Off, floatingWindowRoute(false, FloatingWindowMode.ID_LMO))
        assertEquals(FloatingWindowRoute.Off, floatingWindowRoute(false, FloatingWindowMode.ID_SYSTEM))
    }

    @Test
    fun onRoutesByMode() {
        assertEquals(FloatingWindowRoute.System, floatingWindowRoute(true, FloatingWindowMode.ID_SYSTEM))
        assertEquals(FloatingWindowRoute.Lmo, floatingWindowRoute(true, FloatingWindowMode.ID_LMO))
    }

    @Test
    fun unknownIdFallsBackToSystem() {
        assertEquals(FloatingWindowRoute.System, floatingWindowRoute(true, "nonsense"))
        assertEquals(FloatingWindowRoute.System, floatingWindowRoute(true, null))
    }

    @Test
    fun defaultsAreOnAndSystem() {
        val d = IslaSettings.Default
        assertTrue(d.floatingWindowEnabled)
        assertEquals(FloatingWindowMode.ID_SYSTEM, d.floatingWindowMode)
    }

    @Test
    fun settingsSurviveBackupAndBadValuesAreRepaired() {
        val s = IslaSettings(floatingWindowEnabled = false, floatingWindowMode = FloatingWindowMode.ID_LMO)
        val back = IslaSettings.fromJson(s.toJson(appVersion = "1"))
        assertEquals(false, back.floatingWindowEnabled)
        assertEquals(FloatingWindowMode.ID_LMO, back.floatingWindowMode)
        val bad = IslaSettings.fromJson(s.copy(floatingWindowMode = "x").toJson(appVersion = "1"))
        assertEquals(FloatingWindowMode.ID_SYSTEM, bad.floatingWindowMode)
    }
}
