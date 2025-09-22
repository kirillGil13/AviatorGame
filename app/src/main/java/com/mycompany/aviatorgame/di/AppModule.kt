package com.mycompany.aviatorgame.di

import android.content.Context
import com.mycompany.aviatorgame.data.local.BillingManager
import com.mycompany.aviatorgame.data.local.PreferencesManager
import com.mycompany.aviatorgame.data.repository.GameRepository
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
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context
    ): BillingManager = BillingManager(context)

    @Provides
    @Singleton
    fun provideGameRepository(
        preferencesManager: PreferencesManager,
        billingManager: BillingManager
    ): GameRepository = GameRepository(preferencesManager, billingManager)
}