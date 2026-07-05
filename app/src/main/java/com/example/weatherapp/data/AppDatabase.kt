package com.example.weatherapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserSettings::class, WeatherData::class, ForecastData::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun weatherDataDao(): WeatherDataDao
    abstract fun forecastDataDao(): ForecastDataDao
}