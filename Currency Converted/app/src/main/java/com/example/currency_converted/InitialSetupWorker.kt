package com.example.currency_converted

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.currency_converted.data.remote.MyDatabaseHelper
import com.example.currency_converted.data.remote.remote.APIService
import com.example.currency_converted.data.remote.remote.ApiUtils
import com.example.currency_converted.model.SupportedCodes
import kotlinx.coroutines.delay

class InitialSetupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val apiService: APIService = ApiUtils.getApiService()
    private val prefsHelper = PreferencesHelper(context)

    override suspend fun doWork(): Result {
        return try {
            // Only fetch supported codes if it's first launch
            if (prefsHelper.isFirstLaunch()) {
                val supportedCodes = fetchSupportedCodes()
                if (supportedCodes != null) {
                    saveSupportedCodes(supportedCodes)
                    prefsHelper.setFirstLaunchComplete()
                }
            }
            // Then fetch conversion rates for each supported code
            fetchAllConversionRates()
            Result.success()
        } catch (e: Exception) {
            Log.e("InitialSetupWorker", "Error: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun fetchSupportedCodes(): SupportedCodes? {
        return try {
            apiService.getSupportedCodes()
        } catch (e: Exception) {
            Log.e("InitialSetupWorker", "Failed to fetch supported codes: ${e.message}")
            null
        }
    }

    private fun saveSupportedCodes(codes: SupportedCodes) {
        val dbHelper = MyDatabaseHelper(applicationContext)
        dbHelper.addSupportedCodes(codes)
    }

    private suspend fun fetchAllConversionRates() {
        val dbHelper = MyDatabaseHelper(applicationContext)
        val supportedCodes = dbHelper.getAllSupportedCodes()

        supportedCodes.forEach { code ->
            try {
                val rates = apiService.getConversionRates(code)
                if (rates != null) {
                    dbHelper.addConversionRates(rates)
                }
                // Add a small delay to avoid overwhelming the API
                delay(1000)
            } catch (e: Exception) {
                Log.e("InitialSetupWorker", "Failed to fetch rates for $code: ${e.message}")
            }
        }
    }
}
