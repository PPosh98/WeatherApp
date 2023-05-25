package com.shah.myweatherapp.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shah.myweatherapp.model.weather.WeatherModel

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = false)
    val weatherModel: WeatherModel,
    val query: String? = null
    ) {
    var cityName: String = weatherModel.name
    var latitude: Double = weatherModel.coord.lat
    var longitude: Double = weatherModel.coord.lon
    var icon: String = weatherModel.weather[0].icon
    var temperature: Double = weatherModel.main.temp
    var condition: String = weatherModel.weather[0].main
}