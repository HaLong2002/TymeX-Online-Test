package com.example.currency_converted.data.remote.remote

import com.example.currency_converted.util.const_variables

class ApiUtils {

    companion object {
        private const val BASE_URL = "https://v6.exchangerate-api.com/v6/${const_variables.API_KEY}/"

        fun getApiService(): APIService {
            return RetrofitClient.getClient(BASE_URL).create(APIService::class.java)
        }
    }
}