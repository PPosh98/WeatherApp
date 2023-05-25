package com.shah.myweatherapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.shah.myweatherapp.api.FetchAPI
import com.shah.myweatherapp.model.weather.CoordModel
import com.shah.myweatherapp.model.weather.MainModel
import com.shah.myweatherapp.model.weather.WeatherModel
import com.shah.myweatherapp.model.weather.WeatherModelX
import com.shah.myweatherapp.roomdb.WeatherDAO
import com.shah.myweatherapp.roomdb.WeatherEntity
import com.shah.myweatherapp.util.Resource
import com.shah.myweatherapp.util.isLoading
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryImplTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: RepositoryImpl
    private val fetchAPI: FetchAPI = mockk()
    private val weatherDAO: WeatherDAO = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = RepositoryImpl(fetchAPI, weatherDAO)
    }

    @Test
    fun `getWeather returns local data if available`() = runTest {
        // Given
        val cityName = "london"
        val weatherModel = WeatherModel(7, MainModel(5.5), CoordModel(6.2, 6.9), "London", listOf(WeatherModelX("hot", "icon", 2, "very hot")))
        val weatherEntity = WeatherEntity(weatherModel, cityName)
        coEvery { weatherDAO.readWeatherByCity(cityName) } returns weatherEntity

        // When
        val result = repository.getWeatherByCity(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Success -> assertEquals(weatherModel, resource.data)
                else -> fail("Unexpected resource type")
            }
        }
        coVerify(exactly = 0) { fetchAPI.getWeatherByCity(any()) }
    }

    @Test
    fun `getWeather returns remote data if local data is not available`() = runTest {
        // Given
        val cityName = "london"
        val weatherModel = WeatherModel(7, MainModel(5.5), CoordModel(6.2, 6.9), "London", listOf(WeatherModelX("hot", "icon", 2, "very hot")))
        val weatherEntity = WeatherEntity(weatherModel, cityName)
        coEvery { weatherDAO.readWeatherByCity(cityName) } returns null
        coEvery { fetchAPI.getWeatherByCity(cityName) } returns Response.success(weatherModel)

        // When
        val result = repository.getWeatherByCity(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Success -> assertEquals(weatherModel, resource.data)
                else -> fail("Unexpected resource type")
            }
        }

        coVerify { fetchAPI.getWeatherByCity(cityName) }
        coVerify { weatherDAO.insertWeather(weatherEntity) }
    }

    @Test
    fun `getWeather returns error if remote data is not successful`() = runTest {
        // Given
        val cityName = "Berlin"
        coEvery { weatherDAO.readWeatherByCity(cityName) } returns null
        coEvery { fetchAPI.getWeatherByCity(cityName) } returns Response.error(404, "Not found".toResponseBody("text/plain".toMediaTypeOrNull()))

        // When
        val result = repository.getWeatherByCity(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Error -> assertEquals("Not found", resource.message)
                else -> fail("Unexpected resource type")
            }
        }
    }

    @Test
    fun `getWeather returns error if remote data throws exception`() = runTest {
        // Given
        val cityName = "Tokyo"
        coEvery { weatherDAO.readWeatherByCity(cityName) } returns null
        coEvery { fetchAPI.getWeatherByCity(cityName) } throws IOException("Network error")

        // When
        val result = repository.getWeatherByCity(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Error -> assertEquals("Couldn't load data", resource.message)
                else -> fail("Unexpected resource type")
            }
        }
    }
}
