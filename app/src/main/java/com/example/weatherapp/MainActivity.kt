package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.viewmodel.WeatherViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Job
import androidx.room.Room
import com.example.weatherapp.data.AppDatabase
import com.example.weatherapp.data.ForecastData
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.model.ForecastResponse
import com.example.weatherapp.model.WeatherResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.nestedscroll.nestedScroll

class MainActivity : ComponentActivity() {
    private val apiKey = "1f6fe88d2fbb11158ef2b5d83d7439ed";
    private val userTypeState = mutableStateOf("normal")
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "weatherapp.db"
        ).build()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                //var userType by remember { mutableStateOf("normal") }
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                LaunchedEffect(Unit) {
                    db.userSettingsDao().getSettings()?.let {
                        userTypeState.value = it.userType
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon = {},
                        actions = {
                            IconButton(onClick = {
                                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                                startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ustawienia"
                                )
                            }
                            IconButton(onClick = {
                                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                                startActivity(intent)
                            }) {
                                Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "O aplikacji"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                ) { innerPadding ->
                    WeatherScreen(this.apiKey, db, modifier = Modifier.padding(innerPadding).fillMaxSize(), userTypeState.value)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            db.userSettingsDao().getSettings()?.let {
                userTypeState.value = it.userType
            }
        }
    }
}
@SuppressLint("MissingPermission")
@Composable
fun WeatherScreen(apiKey: String, db: AppDatabase, modifier: Modifier, userType: String) {
    val scope = rememberCoroutineScope()
    val viewModel: WeatherViewModel = viewModel()
    val weather = viewModel.weatherData.observeAsState()
    val forecast = viewModel.forecastData.observeAsState()

    var searchCity by rememberSaveable { mutableStateOf("") }
    var lastCityRequested by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var lastLocationRequested by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var lastGpsRequestTime by remember { mutableStateOf(0L) }
    val gpsCooldownMs = 10_000L

    val gson = remember { Gson() }
    var isFromCache by remember { mutableStateOf(false) }
    val error = viewModel.error.observeAsState()

    LaunchedEffect(Unit) {
        val savedWeather = db.weatherDataDao().getWeather()
        val savedForecast = db.forecastDataDao().getForecast()
        savedWeather?.let {
            val weatherObj = Gson().fromJson(it.weatherJson, WeatherResponse::class.java)
            viewModel.weatherData.value = weatherObj
        }
        savedForecast?.let {
            val forecastObj = Gson().fromJson(it.forecastJson, ForecastResponse::class.java)
            viewModel.forecastData.value = forecastObj
        }
        //if(savedWeather != null && savedForecast != null)
            //isFromCache = true
        if(!viewModel.initialLoad) {
            isFromCache = savedWeather != null && savedForecast != null
            viewModel.initialLoad = true
        }
        else
            isFromCache = false
    }

    LaunchedEffect(weather.value) {
        weather.value?.let {
            val current = weather.value ?: return@LaunchedEffect
            val saved = db.weatherDataDao().getWeather()

            if(saved?.weatherJson != gson.toJson(current))
                db.weatherDataDao().insertWeather(WeatherData(1, gson.toJson(current)))
        }
    }
    LaunchedEffect(forecast.value) {
        forecast.value?.let {
            val current = forecast.value ?: return@LaunchedEffect
            val saved = db.forecastDataDao().getForecast()

            if(saved?.forecastJson != gson.toJson(current))
                db.forecastDataDao().insertForecast(ForecastData(1, gson.toJson(current)))
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                errorMessage = "Brak pozwolenia na lokalizację"
                return@rememberLauncherForActivityResult
            }

            isLoading = true //true
            errorMessage = null
            isFromCache = false

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastCityRequested = ""
                        viewModel.loadWeatherByLocation(location.latitude, location.longitude, apiKey)
                        viewModel.loadForecastByLocation(location.latitude, location.longitude, apiKey)
                    } else {
                        errorMessage = "Nie udało się pobrać lokalizacji"
                        isFromCache = true;
                    }
                }
                .addOnFailureListener {
                    errorMessage = "Błąd pobierania lokalizacji"
                }
        }
    )

    /*LaunchedEffect(weather.value, errorMessage) {
        isLoading = false
    }*/

    LazyColumn(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        item {
            OutlinedTextField(
                value = searchCity,
                onValueChange = { newValue ->
                    searchCity = newValue
                    debounceJob?.cancel()
                    debounceJob = scope.launch {
                        delay(800)
                        if (searchCity.isNotBlank() && searchCity != lastCityRequested) {
                            lastCityRequested = searchCity
                            isLoading = true //true
                            errorMessage = null
                            searchCity = searchCity.trim()
                            isFromCache = false
                            try {
                                viewModel.weatherData.value = null
                                viewModel.forecastData.value = null
                                viewModel.loadWeather(searchCity, apiKey)
                                viewModel.loadForecast(searchCity, apiKey)
                            } catch (e: Exception) {
                                errorMessage = "Błąd ładowania pogody"
                                isFromCache = true
                            }
                        }
                    }
                },
                label = { Text("Wpisz miasto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if(isLoading) {
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Ładowanie...",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            else if(error.value != null) {
                AnimatedVisibility(
                    visible = error.value != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut()
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(error.value!!, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            else if(isFromCache) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("⚠\uFE0F Dane z pamięci (mogą być nieaktualne)", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }

            LaunchedEffect(isLoading) {
                if(isLoading) {
                    //delay(10_000L)
                    isLoading = false
                    //if(weather.value == null)
                        //errorMessage = "Błąd pobierania danych"
                }
            }

            Button(onClick = {
                viewModel.clearError()
                isLoading = true //true
                errorMessage = null
                searchCity = ""
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text("Pobierz pogodę z GPS")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(errorMessage!!)
                weather.value != null -> {
                    val temp = weather.value!!.main.temp
                    val desc = viewModel.translateDesc(weather.value!!.weather.firstOrNull()?.description ?: "")
                    val icon = weather.value!!.weather.firstOrNull()?.icon ?: "01d"
                    val iconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
                    val cityName = weather.value!!.name
                    val forecastList = forecast.value?.list
                    val mainWeather = weather.value!!.weather
                    val weatherType = viewModel.translateType(mainWeather.firstOrNull()?.main ?: "")
                    AnimatedVisibility(
                        visible = weather.value != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Card(modifier = Modifier.fillMaxSize()) {

                            Text(
                                text = "Miasto: $cityName",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Temperatura: $temp°C",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = weatherType,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Opis: $desc",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(iconUrl),
                                    contentDescription = "Ikona pogody",
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                    when (userType) {
                        "skater" -> {
                                Card(modifier = Modifier.fillMaxSize()) {

                                    val windSpeed = weather.value!!.wind.speed
                                    val humidity = weather.value!!.main.humidity
                                    val visibility = weather.value!!.visibility ?: 0
                                    val rain = weather.value!!.rain?.oneHour ?: 0.0
                                    val type = weather.value!!.weather.firstOrNull()?.main ?: ""

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("🛼 Dla rolkarza:", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                    Text(
                                        "Stan nawierzchni: " +
                                                when {
                                                    rain > 0.0 || type == "Snow" -> "Mokra / śliska ❌"
                                                    humidity > 85 -> "Możliwa wilgoć ⚠️"
                                                    else -> "Sucha ✅"
                                                },
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Opady (ostatnia godzina): $rain mm", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                    Text(
                                        "Wiatr: $windSpeed m/s " +
                                                when {
                                                    windSpeed > 8 -> "(silny ❌)"
                                                    windSpeed > 4 -> "(umiarkowany ⚠️)"
                                                    else -> "(lekki ✅)"
                                                },
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Widoczność: $visibility m", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                    val safetyScore = when {
                                        rain > 0.0 || windSpeed > 10 || type == "Snow" -> "NIEBEZPIECZNIE ❌"
                                        windSpeed > 6 || humidity > 90 -> "ŚREDNIO ⚠️"
                                        else -> "DOBRZE ✅"
                                    }

                                    Text("Warunki jazdy: $safetyScore", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                }
                        }

                        "runner" -> {
                            Card(modifier = Modifier.fillMaxSize()) {
                                    val humidity = weather.value!!.main.humidity
                                    val windSpeed = weather.value!!.wind.speed
                                    val feelsLike = weather.value!!.main.feels_like
                                    val rain = weather.value!!.rain?.oneHour ?: 0.0

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("🏃 Dla biegacza:", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                    Text("Temperatura odczuwalna: $feelsLike°C", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                    Text(
                                        "Wilgotność: $humidity% " +
                                                when {
                                                    humidity > 85 -> "(bardzo wysoka ❌)"
                                                    humidity > 65 -> "(umiarkowana ⚠️)"
                                                    else -> "(komfortowa ✅)"
                                                },
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Wiatr: $windSpeed m/s", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                    Text(
                                        "Opady: $rain mm " +
                                                when {
                                                    rain > 5 -> "(ulewa ❌)"
                                                    rain > 0 -> "(lekki deszcz ⚠️)"
                                                    else -> "(brak ✅)"
                                                },
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    val comfort = when {
                                        feelsLike > 30 || humidity > 90 -> "CIĘŻKO ❌"
                                        feelsLike < 0 -> "ZIMNO ⚠️"
                                        rain > 5 -> "SŁABE WARUNKI ❌"
                                        else -> "DOBRE ✅"
                                    }

                                    Text("Warunki do biegania: $comfort", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }

                        }

                        else -> {}
                    }
                    if (forecastList != null) {
                        Card(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Pogoda długoterminowa: ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    forecastList?.forEach { w ->
                        Card(modifier = Modifier.fillMaxSize()) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            val dateTime = LocalDateTime.parse(w.dt_txt, formatter)

                            val forecastDay = dateTime.toLocalDate().toString()
                            val forecastTime = dateTime.toLocalTime().toString()
                            val forecastTemp = w.main.temp
                            val forecastDesc = viewModel.translateDesc(w.weather.firstOrNull()?.description ?: "")
                            val forecastType = viewModel.translateType(w.weather.firstOrNull()?.main ?: "")

                            Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                                Text(
                                    text = "\uD83D\uDDD3\uFE0F: $forecastDay",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "⏰: $forecastTime",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(text = "Temperatura: $forecastTemp°C", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                Text(text = forecastType, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                Text(text = "Opis: $forecastDesc", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                else -> Text("Wpisz miasto lub użyj GPS", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}