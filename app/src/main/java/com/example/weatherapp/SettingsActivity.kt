package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.weatherapp.data.AppDatabase
import com.example.weatherapp.data.UserSettings
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "weatherapp.db").build()

        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedType by rememberSaveable { mutableStateOf("") }

                    LaunchedEffect(Unit) {
                        if(selectedType == "") {
                            val settings = db.userSettingsDao().getSettings()
                            selectedType = settings?.userType ?: ""
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Wybierz typ użytkownika:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        listOf("normal", "skater", "runner").forEach { type ->
                            Row(modifier = Modifier.width(160.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(type)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.userSettingsDao()
                                    .saveSettings(UserSettings(id = 1, userType = selectedType))
                                finish()
                            }
                        }) {
                            Text("Zapisz i wróć")
                        }
                    }
                }
            }
        }
    }
}