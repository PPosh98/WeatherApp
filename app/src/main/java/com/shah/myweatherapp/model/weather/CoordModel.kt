package com.shah.myweatherapp.model.weather

import com.google.gson.annotations.SerializedName

data class CoordModel(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)
