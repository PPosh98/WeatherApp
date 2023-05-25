package com.shah.myweatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shah.myweatherapp.model.weather.WeatherModel
import com.shah.myweatherapp.repository.Repository
import com.shah.myweatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository): ViewModel(){

    private val _weather : MutableStateFlow<Resource<WeatherModel>> = MutableStateFlow(Resource.Loading())
    val weather: StateFlow<Resource<WeatherModel>> get() = _weather

    fun getWeatherByCity(cityName: String) {
        repository.getWeatherByCity(cityName)
            .onEach { _weather.value = it }
            .launchIn(viewModelScope)
    }

    fun getWeatherByCoords(latitude: Double?, longitude: Double?) {
        repository.getWeatherByCoords(latitude, longitude)
            .onEach { _weather.value = it }
            .launchIn(viewModelScope)
    }
}