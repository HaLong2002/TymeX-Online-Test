package com.example.currency_converted.data.remote

class ApiUtils {

    companion object {
        private val BASE_URL = "https://api.exchangeratesapi.io/v1/"

        fun getApiService(): APIService {
            return RetrofitClient.getClient(BASE_URL).create(APIService::class.java)
        }
    }
}