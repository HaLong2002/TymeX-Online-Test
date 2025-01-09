package com.example.currency_converted.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.currency_converted.data.remote.MyDatabaseHelper
import com.example.currency_converted.data.remote.remote.APIService
import com.example.currency_converted.data.remote.remote.ApiUtils
import com.example.currency_converted.model.ConversionRates
import kotlinx.coroutines.delay

class UpdateConversionRatesWorker(appContext: Context,
                                params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val mAPIService: APIService = ApiUtils.getApiService()

    override suspend fun doWork(): Result {
        return try {
            val dbHelper = MyDatabaseHelper(applicationContext)
            val supportedCodes = dbHelper.getAllSupportedCodes()

            val newRates = mutableListOf<ConversionRates>()

            supportedCodes.forEach { code ->
                var retries = 3
                var success = false
                while (retries > 0 && !success) {
                    try {
                        val rates = mAPIService.getConversionRates(code)
                        if (rates != null) {
                            newRates.add(rates) // Collect new rates
                            success = true
                        }
                        delay(1000) // Prevent API rate limits
                    } catch (e: Exception) {
                        retries--
                        Log.e("UpdateWorker", "Failed to fetch rates for $code: ${e.message}. Retries left: $retries")
                        if (retries == 0) Log.e("UpdateWorker", "Giving up on $code")
                    }
                }
            }

            // Update database only if new rates were fetched
            if (newRates.isNotEmpty()) {
                dbHelper.clearAllConversionRates() // Clear old data
                newRates.forEach { newRate -> dbHelper.addConversionRates(newRate)}
            } else {
                Log.w("UpdateWorker", "No new rates fetched. Retaining old data.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateWorker", "Error: ${e.message}")
            Result.retry()
        }
    }

}