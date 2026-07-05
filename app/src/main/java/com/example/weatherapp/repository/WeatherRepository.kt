package com.example.weatherapp.repository

import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.model.ForecastResponse

class WeatherRepository {
    suspend fun getWeather(city: String, apiKey: String): WeatherResponse {
        return RetrofitInstance.api.getWeatherByCity(city, apiKey)
    }

    suspend fun getWeatherByLocation(lat: Double, lon: Double, apiKey: String): WeatherResponse {
        return RetrofitInstance.api.getWeatherByLocation(lat, lon, apiKey)
    }
    suspend fun getForecast(city: String, apiKey: String): ForecastResponse {
        return RetrofitInstance.api.getForecast(city, apiKey)
    }
    suspend fun getForecastByLocation(lat: Double, lon: Double, apiKey: String): ForecastResponse {
        return RetrofitInstance.api.getForecastByLocation(lat, lon, apiKey)
    }
}