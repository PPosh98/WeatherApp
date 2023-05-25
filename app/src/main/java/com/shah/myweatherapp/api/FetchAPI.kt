package com.shah.myweatherapp.api

import com.shah.myweatherapp.api.APIReference.API_KEY
import com.shah.myweatherapp.model.weather.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FetchAPI {

    @GET("data/2.5/weather")
    suspend fun getWeatherByCoords(
        @Query("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ) : Response<WeatherModel>

    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ) : Response<WeatherModel>
}