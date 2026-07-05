package com.example.weatherapp.data

import androidx.room.*

@Dao
interface ForecastDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecastData: ForecastData)

    @Query("SELECT * FROM forecast_data WHERE id = 1")
    suspend fun getForecast(): ForecastData?

    @Query("DELETE FROM forecast_data")
    suspend fun clearForecast()
}