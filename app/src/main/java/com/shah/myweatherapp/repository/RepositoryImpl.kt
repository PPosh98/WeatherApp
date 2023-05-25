package com.shah.myweatherapp.repository

import com.shah.myweatherapp.api.FetchAPI
import com.shah.myweatherapp.model.weather.WeatherModel
import com.shah.myweatherapp.roomdb.WeatherDAO
import com.shah.myweatherapp.roomdb.WeatherEntity
import com.shah.myweatherapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val fetchAPI: FetchAPI,
    private val weatherDAO: WeatherDAO
) :
    Repository {

    override fun getWeatherByCity(cityNameQuery: String): Flow<Resource<WeatherModel>> {
        return flow {
            emit(Resource.Loading(true))
            val weatherFromLocal = weatherDAO.readWeatherByCity(cityNameQuery)
            val shouldJustLoadFromCache =
                weatherFromLocal != null && cityNameQuery.isNotBlank()
            if (shouldJustLoadFromCache) {
                emit(Resource.Success(weatherFromLocal?.weatherModel))
                return@flow
            }
            try {
                val response = fetchAPI.getWeatherByCity(cityNameQuery)
                if (response.isSuccessful) {
                    val weather = response.body() as WeatherModel
                    weatherDAO.insertWeather(WeatherEntity(weather, cityNameQuery))
                    emit(Resource.Success(weather))
                } else {
                    emit(Resource.Error("Not found"))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            }
        }
    }

    override fun getWeatherByCoords(latitude: Double?, longitude: Double?): Flow<Resource<WeatherModel>> {
        return flow {
            emit(Resource.Loading(true))
            val weatherFromLocal = weatherDAO.readWeatherByCoord(latitude, longitude)
            val shouldJustLoadFromCache = weatherFromLocal != null
            if (shouldJustLoadFromCache) {
                emit(Resource.Success(weatherFromLocal?.weatherModel))
                return@flow
            }
            try {
                val response = fetchAPI.getWeatherByCoords(latitude, longitude)
                if (response.isSuccessful) {
                    val weather = response.body() as WeatherModel
                    weatherDAO.insertWeather(WeatherEntity(weather))
                    emit(Resource.Success(weather))
                } else {
                    emit(Resource.Error("Not found"))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            }
        }
    }
}