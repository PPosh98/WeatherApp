package com.shah.myweatherapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.shah.myweatherapp.model.weather.MainModel
import com.shah.myweatherapp.model.weather.WeatherModel
import com.shah.myweatherapp.repository.Repository
import com.shah.myweatherapp.repository.RepositoryImpl
import com.shah.myweatherapp.util.Resource
import com.shah.myweatherapp.util.isError
import com.shah.myweatherapp.util.isSuccess
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository = mockk<Repository>()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // mock the main dispatcher
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher
    }

    @Test
    fun `getWeather should emit success when repository returns data`() = runTest {
        // Given
        val cityName = "london"
        val data = WeatherModel(7, MainModel(5.5), "London", listOf())
        coEvery { repository.getWeather(cityName) } returns flowOf(Resource.Success(data))

        // When
        viewModel.getWeather(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(true, viewModel.weather.value.isSuccess())
        assertEquals(data, viewModel.weather.value.data)
    }

    @Test
    fun `getWeather should emit error when repository returns error`() = runTest {
        // Given
        val cityName = "london"
        val exception = Exception("Network error")
        coEvery { repository.getWeather(cityName) } returns flowOf(Resource.Error(exception.message.toString()))

        // When
        viewModel.getWeather(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(true, viewModel.weather.value.isError())
        assertEquals("Network error", viewModel.weather.value.message)
    }
}


