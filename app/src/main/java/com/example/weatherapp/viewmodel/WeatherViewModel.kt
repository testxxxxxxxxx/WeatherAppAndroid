package com.example.weatherapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.ForecastResponse
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    val weatherData = MutableLiveData<WeatherResponse>()
    val forecastData = MutableLiveData<ForecastResponse>()
    val error = MutableLiveData<String?>(null)
    var initialLoad = false

    fun loadWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val result = repository.getWeather(city, apiKey)
                weatherData.value = result
                error.postValue(null)
            }
            catch(e: Exception) {
                e.printStackTrace()
                error.postValue("Nie znaleziono miasta")
            }
        }
    }

    fun loadWeatherByLocation(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                withTimeout(10_000L) {
                    val result = repository.getWeatherByLocation(lat, lon, apiKey)
                    weatherData.value = result
                }
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadForecast(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val result = repository.getForecast(city, apiKey)
                forecastData.value = result
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun loadForecastByLocation(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                withTimeout(10_000L) {
                    val result = repository.getForecastByLocation(lat, lon, apiKey)
                    forecastData.value = result
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun clearError() {
        this.error.value = null
    }
    fun translateType(type: String): String {
        return when(type) {
            "Rain" -> "Deszcz 🌧️"
            "Snow" -> "Śnieg ❄️"
            "Clear" -> "Słonecznie ☀️"
            "Clouds" -> "Chmury ☁️"
            else -> type
        }
    }
    fun translateDesc(desc: String): String {
        return when (desc.lowercase()) {
            "clear sky" -> "Bezchmurnie ☀️"
            "few clouds" -> "Małe zachmurzenie 🌤️"
            "scattered clouds" -> "Zachmurzenie umiarkowane ☁️"
            "broken clouds" -> "Duże zachmurzenie ☁️"
            "overcast clouds" -> "Całkowite zachmurzenie ☁️"

            "light rain" -> "Lekki deszcz 🌦️"
            "moderate rain" -> "Umiarkowany deszcz 🌧️"
            "heavy intensity rain" -> "Silny deszcz 🌧️"
            "very heavy rain" -> "Bardzo silny deszcz 🌧️"
            "extreme rain" -> "Ekstremalny deszcz ⛈️"

            "light snow" -> "Lekki śnieg 🌨️"
            "snow" -> "Śnieg ❄️"
            "heavy snow" -> "Silny śnieg ❄️"

            "thunderstorm" -> "Burza ⛈️"
            "thunderstorm with rain" -> "Burza z deszczem ⛈️🌧️"

            "mist" -> "Mgła 🌫️"
            "fog" -> "Gęsta mgła 🌫️"
            "haze" -> "Zamglenie 🌫️"

            else -> desc
        }
    }
}