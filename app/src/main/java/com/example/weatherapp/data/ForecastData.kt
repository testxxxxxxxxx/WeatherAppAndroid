package com.example.weatherapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecast_data")
data class ForecastData (
    @PrimaryKey val id: Int = 1,
    val forecastJson: String
)