package com.shah.myweatherapp.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather WHERE :latitude LIKE latitude AND :longitude LIKE longitude OR " +
            "latitude LIKE :latitude AND longitude LIKE :longitude")
    suspend fun readWeatherByCoord(latitude: Double?, longitude: Double?) : WeatherEntity?

    @Query("SELECT * FROM weather WHERE :query LIKE '%' || query || '%' OR query LIKE '%' || :query || '%'")
    suspend fun readWeatherByCity(query: String) : WeatherEntity?

    @Query("DELETE FROM weather")
    fun delete()
}