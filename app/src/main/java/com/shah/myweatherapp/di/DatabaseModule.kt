package com.shah.myweatherapp.di

import android.content.Context
import androidx.room.Room
import com.shah.myweatherapp.roomdb.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideRoomInstance(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        WeatherDatabase::class.java,
        "WeatherDatabase"
    ).build()

    @Provides
    fun provideWeatherDao(database: WeatherDatabase) = database.weatherDAO()
}