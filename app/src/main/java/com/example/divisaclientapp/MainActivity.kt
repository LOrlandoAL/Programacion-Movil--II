package com.example.divisaclientapp

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClientApp() // Llama a la función principal de la UI
        }
    }

    @Composable
    fun ClientApp() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // Variables de estado para la UI
        var selectedCurrency by remember { mutableStateOf("MXN") } // Divisa seleccionada
        var availableCurrencies by remember { mutableStateOf(listOf<String>()) } // Lista de divisas disponibles
        var selectedPeriod by remember { mutableStateOf("1 Semana") } // Período de tiempo seleccionado
        var exchangeRates by remember { mutableStateOf(emptyList<Pair<String, Float>>()) } // Datos para la gráfica
        var chartKey by remember { mutableStateOf(0) } // Clave para forzar la actualización de la gráfica

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Obtener la lista de divisas disponibles desde el ContentProvider
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                availableCurrencies = fetchAvailableCurrencies(context)
            }
        }

        // Actualizar los datos cuando se cambia la divisa o el período de tiempo
        LaunchedEffect(selectedCurrency, selectedPeriod) {
            coroutineScope.launch {
                val range = getDateRange(selectedPeriod)
                exchangeRates = fetchExchangeRates(context, selectedCurrency, range.first, range.second)
                chartKey++ // Incrementa el key para reconstruir la gráfica
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta que contiene los controles de selección
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Seleccione una divisa:", style = MaterialTheme.typography.headlineMedium)

                    // Menú desplegable para seleccionar la divisa
                    DropdownMenuComponent(
                        label = "Divisa",
                        options = availableCurrencies,
                        selectedOption = selectedCurrency,
                        onOptionSelected = { selectedCurrency = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Seleccione el período de tiempo:", style = MaterialTheme.typography.bodyLarge)

                    // Menú desplegable para seleccionar el período de tiempo
                    DropdownMenuComponent(
                        label = "Período",
                        options = listOf("1 Día", "1 Semana", "1 Mes", "1 Año"),
                        selectedOption = selectedPeriod,
                        onOptionSelected = { selectedPeriod = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para actualizar la gráfica manualmente
                    Button(onClick = {
                        coroutineScope.launch {
                            val range = getDateRange(selectedPeriod)
                            exchangeRates = fetchExchangeRates(context, selectedCurrency, range.first, range.second)
                            chartKey++ // Forzar la actualización de la gráfica
                        }
                    }) {
                        Text("Actualizar Gráfica")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mostrar la gráfica si hay datos, forzando su reconstrucción con `key`
            if (exchangeRates.isNotEmpty()) {
                key(chartKey) {
                    ExchangeRateChart(exchangeRates)
                }
            }
        }
    }

    // Menú desplegable genérico
    @Composable
    fun DropdownMenuComponent(
        label: String,
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Box {
                Button(onClick = { expanded = true }) {
                    Text(text = selectedOption)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Obtiene la lista de divisas disponibles desde el ContentProvider
    fun fetchAvailableCurrencies(context: android.content.Context): List<String> {
        val uri = Uri.parse("content://com.example.syncdivisaapp.provider/exchange_rates")
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        val currenciesSet = mutableSetOf<String>()
        cursor?.use {
            val exchangeRatesIndex = it.getColumnIndex("exchangeRates")
            while (it.moveToNext()) {
                val exchangeRatesJson = it.getString(exchangeRatesIndex)
                val type = object : TypeToken<Map<String, Double>>() {}.type
                val ratesMap: Map<String, Double> = Gson().fromJson(exchangeRatesJson, type)
                currenciesSet.addAll(ratesMap.keys)
            }
        }
        return currenciesSet.toList()
    }

    // Obtiene los valores históricos de la divisa seleccionada
    fun fetchExchangeRates(
        context: android.content.Context,
        currency: String,
        startDate: String,
        endDate: String
    ): List<Pair<String, Float>> {
        val uri = Uri.parse("content://com.example.syncdivisaapp.provider/exchange_rates/$currency/$startDate/$endDate")
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        val ratesList = mutableListOf<Pair<String, Float>>()
        cursor?.use {
            val exchangeRatesIndex = it.getColumnIndex("exchangeRates")
            val lastFetchIndex = it.getColumnIndex("lastFetch")

            while (it.moveToNext()) {
                val exchangeRatesJson = it.getString(exchangeRatesIndex)
                val type = object : TypeToken<Map<String, Double>>() {}.type
                val ratesMap: Map<String, Double> = Gson().fromJson(exchangeRatesJson, type)

                val rate = try {
                    ratesMap[currency]?.toFloat() ?: continue
                } catch (e: NumberFormatException) {
                    Log.e("PARSING_ERROR", "Error al convertir la tasa de cambio: ${e.message}")
                    continue
                }

                val date = it.getString(lastFetchIndex)
                ratesList.add(date to rate)
            }
        }
        return ratesList
    }

    // Calcula el rango de fechas según el período seleccionado
    fun getDateRange(period: String): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val endDate = dateFormat.format(calendar.time)

        when (period) {
            "1 Día" -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            "1 Semana" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            "1 Mes" -> calendar.add(Calendar.MONTH, -1)
            "1 Año" -> calendar.add(Calendar.YEAR, -1)
        }

        val startDate = dateFormat.format(calendar.time)
        return Pair(startDate, endDate)
    }


    @Composable
    fun ExchangeRateChart(rates: List<Pair<String, Float>>) {
        // Filtrar valores repetidos, manteniendo solo cuando el precio cambió
        val filteredRates = rates.fold(mutableListOf<Pair<String, Float>>()) { acc, rate ->
            if (acc.isEmpty() || acc.last().second != rate.second) {
                acc.add(rate)
            }
            acc
        }

        // Convertir los valores a entradas para la gráfica
        val entries = filteredRates.mapIndexed { index, (_, rate) -> Entry(index.toFloat(), rate) }
        val dataSet = LineDataSet(entries, "Historial de Cambio").apply {
            color = android.graphics.Color.BLUE
            setDrawCircles(true)
            circleRadius = 4f
            circleHoleRadius = 2f
            setCircleColor(android.graphics.Color.BLUE)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = android.graphics.Color.BLUE
            fillAlpha = 50
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val lineData = LineData(dataSet)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            AndroidView(factory = { context ->
                LineChart(context).apply {
                    data = lineData
                    description = Description().apply { text = "" }
                    legend.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    xAxis.setDrawLabels(false)
                    xAxis.setDrawGridLines(false)
                    axisLeft.setDrawGridLines(false)
                    axisRight.isEnabled = false
                    invalidate()
                }
            }, modifier = Modifier.fillMaxSize())
        }
    }
}
