package ru.netology.nmedia.google

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GoogleModule {

    @Singleton
    @Provides
    fun  provideGoogleApiAvailability(): GoogleApiAvailability = GoogleApiAvailability.getInstance()
}