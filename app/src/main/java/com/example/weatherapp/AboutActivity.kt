package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LazyColumn (
                        modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        item {
                            Image(
                            painter = painterResource(id = R.drawable.main_icon),
                            contentDescription = "Main icon app",
                            modifier = Modifier.size(120.dp)
                        )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("WeatherApp", style = MaterialTheme.typography.headlineSmall)
                            Text("Wersja 1.0.0")

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            Text(
                                text = "WeatherApp to nowoczesna aplikacja pogodowa, która umożliwia szybkie sprawdzanie aktualnych warunków atmosferycznych oraz prognozy na kolejne dni.\n" +
                                        "\n" +
                                        "Dzięki integracji z lokalizacją GPS oraz wyszukiwaniu miast, użytkownik może w prosty sposób uzyskać potrzebne informacje pogodowe w dowolnym miejscu na świecie.\n" +
                                        "\n" +
                                        "Aplikacja oferuje również spersonalizowane wskazówki dla aktywności takich jak bieganie czy jazda na rolkach.",
                                textAlign = TextAlign.Center
                            )
                        }
                        item {
                            Button(onClick = {finish()}) {
                                Text("Powrót")
                            }
                        }
                    }
                }
            }
        }
    }
}