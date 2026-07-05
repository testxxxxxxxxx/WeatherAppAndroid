package com.example.weatherapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherData (
    @PrimaryKey val id: Int = 1,
    val weatherJson: String
)