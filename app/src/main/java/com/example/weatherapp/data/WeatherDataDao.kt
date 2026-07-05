package com.example.weatherapp.data

import androidx.room.*

@Dao
interface WeatherDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherData: WeatherData)

    @Query("SELECT * FROM weather_data WHERE id = 1")
    suspend fun getWeather(): WeatherData?

    @Query("DELETE FROM weather_data")
    suspend fun clearWeather()
}