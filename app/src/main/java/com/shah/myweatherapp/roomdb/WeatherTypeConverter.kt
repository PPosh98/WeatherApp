package com.shah.myweatherapp.roomdb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shah.myweatherapp.model.weather.WeatherModel

class WeatherTypeConverter {

    var gson = Gson()

    @TypeConverter
    fun weatherToString(weather: WeatherModel): String = gson.toJson(weather)

    @TypeConverter
    fun stringToWeather(data: String): WeatherModel {
        val listType = object : TypeToken<WeatherModel>() {}.type
        return gson.fromJson(data, listType)
    }
}