/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pao.isla.lab.di

import android.content.Context
import com.pao.isla.lab.data.INotificationHistoryRepository
import com.pao.isla.lab.data.INotificationRepository
import com.pao.isla.lab.data.IslaSettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface IslaRepositoriesEntryPoint {
    fun settingsRepository(): IslaSettingsRepository
    fun notificationRepository(): INotificationRepository
    fun notificationHistoryRepository(): INotificationHistoryRepository
}

object IslaRepositories {
    fun settingsRepository(context: Context): IslaSettingsRepository =
        entryPoint(context).settingsRepository()

    fun notificationRepository(context: Context): INotificationRepository =
        entryPoint(context).notificationRepository()

    fun historyRepository(context: Context): INotificationHistoryRepository =
        entryPoint(context).notificationHistoryRepository()

    private fun entryPoint(context: Context): IslaRepositoriesEntryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            IslaRepositoriesEntryPoint::class.java
        )
}
