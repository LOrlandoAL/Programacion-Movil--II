package com.example.syncdivisaapp.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.syncdivisaapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.syncdivisaapp.model.ExchangeRateResponse

class ExchangeRateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        fetchExchangeRates()
        return Result.success()
    }

    private fun fetchExchangeRates() {
        val call = RetrofitClient.apiService.getExchangeRates()

        call.enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    val exchangeRates = response.body()
                    exchangeRates?.let {
                        Log.d("API_WORKER_SUCCESS", "Datos actualizados: ${it.conversion_rates}")
                    }
                } else {
                    Log.e("API_WORKER_ERROR", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                Log.e("API_WORKER_FAILURE", "Fallo en la solicitud: ${t.message}")
            }
        })
    }
}