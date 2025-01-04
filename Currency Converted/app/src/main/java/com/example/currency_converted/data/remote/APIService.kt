package com.example.currency_converted.data.remote

import com.example.currency_converted.model.ExchangeRateResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("/latest?")
    fun getExchangeRates(
        @Query("access_key") access_key: String
    ): Call<ExchangeRateResponse>
}