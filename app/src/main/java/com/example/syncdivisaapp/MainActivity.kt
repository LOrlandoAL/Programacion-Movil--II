package com.example.syncdivisaapp

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
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.syncdivisaapp.model.ExchangeRateResponse
import com.example.syncdivisaapp.network.RetrofitClient
import com.example.syncdivisaapp.ui.theme.SyncDivisaAPPTheme
import com.example.syncdivisaapp.worker.ExchangeRateWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleExchangeRateUpdate()
        setContent {
            SyncDivisaAPPTheme {
                MainScreen()
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
fun MainScreen() {
    var exchangeRate by remember { mutableStateOf("Cargando...") }

    LaunchedEffect(Unit) {
        exchangeRate = fetchExchangeRates() ?: "Error al obtener datos"
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(text = exchangeRate, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

suspend fun fetchExchangeRates(): String? {
    return try {
        val response = RetrofitClient.apiService.getExchangeRates("e752ff2208ffa575854247e8")
        val rate = response.conversion_rates["MXN"]
        Log.d("API_SUCCESS", "Datos recibidos: $rate")
        rate.toString()
    } catch (e: Exception) {
        Log.e("API_FAILURE", "Fallo en la solicitud: ${e.message}")
        null
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SyncDivisaAPPTheme {
        MainScreen()
    }
}
