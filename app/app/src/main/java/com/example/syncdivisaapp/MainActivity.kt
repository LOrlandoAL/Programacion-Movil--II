package com.example.syncdivisaapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.syncdivisaapp.data.database.AppDatabase
import com.example.syncdivisaapp.data.database.ExchangeRateEntity
import com.example.syncdivisaapp.network.RetrofitClient
import com.example.syncdivisaapp.ui.theme.SyncDivisaAPPTheme
import com.example.syncdivisaapp.worker.ExchangeRateWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Llamamos a fetchExchangeRates cuando la aplicación inicia
        lifecycleScope.launch {
            Log.d("MAIN_ACTIVITY", "Llamando a fetchExchangeRates desde MainActivity")
            fetchExchangeRates(applicationContext)
        }

        scheduleExchangeRateUpdate()

        setContent {
            SyncDivisaAPPTheme {
                MainScreen(context = this@MainActivity)
            }
        }
    }

    private fun scheduleExchangeRateUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<ExchangeRateWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "exchange_rate_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun MainScreen(context: Context) {
    var exchangeRates by remember { mutableStateOf("Cargando...") }
    var selectedCurrency by remember { mutableStateOf("USD") } // Moneda seleccionada
    var availableCurrencies by remember { mutableStateOf(listOf<String>()) } // Lista de monedas disponibles
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val db = AppDatabase.getDatabase(context)
            val ratesList = db.exchangeRateDao().getAllRates()

            if (ratesList.isNotEmpty()) {
                val latestRate = ratesList.first()

                val type = object : TypeToken<Map<String, Double>>() {}.type
                val ratesMap: Map<String, Double> = Gson().fromJson(latestRate.exchangeRates, type)

                Log.d("DB_READ", "Última tasa obtenida de la BD: $ratesMap")

                availableCurrencies = ratesMap.keys.toList() // Guardamos las monedas disponibles
                exchangeRates = ratesMap[selectedCurrency]?.toString() ?: "No disponible"
            } else {
                Log.e("DB_EMPTY", "No hay datos en la base de datos")
                exchangeRates = "No hay datos disponibles"
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Seleccione una divisa:", style = MaterialTheme.typography.headlineSmall)

            // Dropdown de selección de divisa
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { expanded = true }) {
                    Text(text = selectedCurrency)
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableCurrencies.forEach { currency ->
                        DropdownMenuItem(text = { Text(text = currency) }, onClick = {
                            selectedCurrency = currency
                            expanded = false

                            coroutineScope.launch {
                                val db = AppDatabase.getDatabase(context)
                                val ratesList = db.exchangeRateDao().getAllRates()
                                if (ratesList.isNotEmpty()) {
                                    val latestRate = ratesList.first()
                                    val type = object : TypeToken<Map<String, Double>>() {}.type
                                    val ratesMap: Map<String, Double> = Gson().fromJson(latestRate.exchangeRates, type)
                                    exchangeRates = ratesMap[selectedCurrency]?.toString() ?: "No disponible"
                                }
                            }
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Muestra el valor de la divisa seleccionada
            Text(
                text = "Valor: $exchangeRates",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}




suspend fun fetchExchangeRates(context: Context): String? {
    return try {
        Log.d("API_CALL", "Llamando a la API para obtener tasas de cambio...")

        val response = RetrofitClient.apiService.getExchangeRates("e752ff2208ffa575854247e8")

        if (response.conversion_rates.isEmpty()) {
            Log.e("API_ERROR", "La API devolvió una lista vacía")
            return null
        }

        val exchangeRatesJson = Gson().toJson(response.conversion_rates).trim()
        Log.d("API_SUCCESS", "Datos recibidos en JSON: $exchangeRatesJson")

        val db = AppDatabase.getDatabase(context)
        val dao = db.exchangeRateDao()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date()) // Última actualización
        val nextFetchTime = dateFormat.format(Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))) // Próxima actualización

        val entity = ExchangeRateEntity(
            exchangeRates = exchangeRatesJson,
            lastFetch = currentTime,
            nextFetch = nextFetchTime
        )

        dao.insertRate(entity)
        Log.d("DB_INSERT", "Guardado en BD: $entity")

        return "Última actualización guardada"
    } catch (e: Exception) {
        Log.e("API_FAILURE", "Error al obtener datos: ${e.message}", e)
        return null
    }
}
