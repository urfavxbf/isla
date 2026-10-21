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
import com.pao.isla.lab.data.NotificationHistoryRepository
import com.pao.isla.lab.data.IslaNotificationRepository
import com.pao.isla.lab.data.IslaSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): IslaSettingsRepository = IslaSettingsRepository(context)

    @Provides
    @Singleton
    fun provideNotificationRepository(): INotificationRepository =
        IslaNotificationRepository()

    @Provides
    @Singleton
    fun provideNotificationHistoryRepository(
        @ApplicationContext context: Context
    ): INotificationHistoryRepository = NotificationHistoryRepository(context)
}
