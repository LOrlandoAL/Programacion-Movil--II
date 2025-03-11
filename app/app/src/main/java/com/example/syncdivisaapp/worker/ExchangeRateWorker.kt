package com.example.syncdivisaapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.syncdivisaapp.data.database.AppDatabase
import com.example.syncdivisaapp.data.database.ExchangeRateEntity
import com.example.syncdivisaapp.fetchExchangeRates
import com.example.syncdivisaapp.network.RetrofitClient
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ExchangeRateWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("WORKER", "Ejecutando worker de sincronización de tasas de cambio...")

            val context = applicationContext
            val result = fetchExchangeRates(context) // Llamamos a la función

            if (result != null) {
                Log.d("WORKER_SUCCESS", "Datos sincronizados correctamente")
                Result.success()
            } else {
                Log.e("WORKER_FAILURE", "No se pudieron obtener los datos")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("WORKER_ERROR", "Error en el Worker: ${e.message}", e)
            Result.failure()
        }
    }

}
