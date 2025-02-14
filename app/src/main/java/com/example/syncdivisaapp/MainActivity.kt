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
        fetchExchangeRates { rate ->
            exchangeRate = "1 USD = $rate MXN"
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = exchangeRate, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

fun fetchExchangeRates(onResult: (String) -> Unit) {
    val call = RetrofitClient.apiService.getExchangeRates()

    call.enqueue(object : retrofit2.Callback<com.example.syncdivisaapp.model.ExchangeRateResponse> {
        override fun onResponse(
            call: retrofit2.Call<com.example.syncdivisaapp.model.ExchangeRateResponse>,
            response: retrofit2.Response<com.example.syncdivisaapp.model.ExchangeRateResponse>
        ) {
            if (response.isSuccessful) {
                val rate = response.body()?.conversion_rates?.get("MXN") ?: "Error"
                onResult(rate.toString())
                Log.d("API_SUCCESS", "Datos recibidos: $rate")
            } else {
                Log.e("API_ERROR", "Error en la respuesta: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(
            call: retrofit2.Call<com.example.syncdivisaapp.model.ExchangeRateResponse>,
            t: Throwable
        ) {
            Log.e("API_FAILURE", "Fallo en la solicitud: ${t.message}")
        }
    })
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SyncDivisaAPPTheme {
        MainScreen()
    }
}
