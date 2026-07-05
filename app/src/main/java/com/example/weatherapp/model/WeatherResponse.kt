package com.example.weatherapp.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: Wind,
    val rain: Rain?,
    val visibility: Int?
)

data class Main(
    val temp: Double,
    //val name: String,
    val humidity: Double,
    @SerializedName("feels_like")
    val feels_like: Double
)

data class Rain (
    @SerializedName("1h")
    val oneHour: Double? = null,
    @SerializedName("3h")
    val threeHour: Double? = null
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)
data class Wind (
    val speed: Double
)