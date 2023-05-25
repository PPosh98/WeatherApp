package com.shah.myweatherapp.repository

import com.shah.myweatherapp.model.weather.WeatherModel
import com.shah.myweatherapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface Repository {

    fun getWeatherByCity(cityName: String) : Flow<Resource<WeatherModel>>

    fun getWeatherByCoords(latitude: Double?, longitude: Double?) : Flow<Resource<WeatherModel>>
}