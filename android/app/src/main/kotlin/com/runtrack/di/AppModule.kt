package com.runtrack.di

import com.runtrack.data.api.RetrofitClient
import com.runtrack.data.api.ShoeService
import com.runtrack.data.api.WeatherService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideShoeService(): ShoeService = RetrofitClient.shoeService

    @Provides
    @Singleton
    fun provideWeatherService(): WeatherService = RetrofitClient.weatherService
}
