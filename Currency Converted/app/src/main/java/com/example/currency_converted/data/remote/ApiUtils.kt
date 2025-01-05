package com.example.currency_converted.data.remote

class ApiUtils {

    companion object {
        private val API_KEY = "8c4f99e2c691a3a2f2892b2d"
        private val BASE_URL = "https://v6.exchangerate-api.com/v6/${API_KEY}/"

        fun getApiService(): APIService {
            return RetrofitClient.getClient(BASE_URL).create(APIService::class.java)
        }
    }
}