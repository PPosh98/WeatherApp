package com.shah.myweatherapp.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.shah.myweatherapp.R
import com.shah.myweatherapp.databinding.ActivityMainBinding
import com.shah.myweatherapp.util.Resource
import com.shah.myweatherapp.workers.DatabaseClearerWorker
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val sharedPref by lazy {
        getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private var currentLocation: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        val uploadWorkRequest =
            PeriodicWorkRequestBuilder<DatabaseClearerWorker>(Duration.ofMinutes(1)).build()
        WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)

        subscribeObserver()

        setSearchSubmitListener()

        loadLastSearched()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                viewModel.getWeatherByCoords(roundTo4dp(location.latitude), roundTo4dp(location.longitude))
            } else {
                Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to access weather data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadLastSearched() {
        val defaultValue = getString(R.string.saved_search_default)
        val searchQuery = sharedPref.getString(getString(R.string.saved_search_key), defaultValue)

        if (searchQuery != defaultValue) {
            if (searchQuery != null) {
                viewModel.getWeatherByCity(searchQuery)
            }
        }
    }

    private fun setSearchSubmitListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.getWeatherByCity(query)

                    with(sharedPref.edit()) {
                        putString(getString(R.string.saved_search_key), query)
                        apply()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun subscribeObserver() {
        lifecycleScope.launch {
            viewModel.weather.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        Log.i("API Response: ", "Loading...")
                    }
                    is Resource.Success -> {
                        Log.i("API Response: ", "Success")
                        Picasso.get()
                            .load("https://openweathermap.org/img/wn/${state.data?.weather?.get(0)?.icon}@4x.png")
                            .into(binding.icon)
                        binding.cityName.text = state.data?.name
                        binding.temperature.text = "${state.data?.main?.temp?.roundToInt()}\u00B0"
                        binding.condition.text = state.data?.weather?.get(0)?.main
                    }
                    is Resource.Error -> {
                        Log.i("API Response: ", "Error -> ${state.message}")
                        Toast.makeText(
                            this@MainActivity,
                            "Sorry, no results found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun roundTo4dp(number: Double): Double {
        val bigDecimal = BigDecimal(number).setScale(4, RoundingMode.HALF_EVEN)
        return bigDecimal.toDouble()
    }
}