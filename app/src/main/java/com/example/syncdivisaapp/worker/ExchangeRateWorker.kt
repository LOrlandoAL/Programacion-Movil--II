package com.example.syncdivisaapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.syncdivisaapp.data.database.AppDatabase
import com.example.syncdivisaapp.data.database.ExchangeRateEntity
import com.example.syncdivisaapp.network.ApiService
import com.example.syncdivisaapp.network.RetrofitClient
import retrofit2.HttpException

class ExchangeRateWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.exchangeRateDao()

        return try {
            // Hacemos la llamada a la API dentro de una coroutine
            val response = RetrofitClient.apiService.getExchangeRates("e752ff2208ffa575854247e8")

            val ratesList = response.conversion_rates.map { (currency, rate) ->
                ExchangeRateEntity(currency = currency, rate = rate)
            }

            dao.deleteAllRates() // Eliminamos los datos previos
            dao.insertRates(ratesList) // Insertamos los nuevos datos


            Result.success()
        } catch (e: HttpException) {
            Result.retry() // Reintentar si hay error HTTP
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure() // Falla si ocurre otro error
        }
    }
}
