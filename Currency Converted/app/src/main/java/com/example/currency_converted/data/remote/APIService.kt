package com.example.currency_converted.data.remote

import com.example.currency_converted.model.CodesResponse
import com.example.currency_converted.model.ExchangeRateResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {
    @GET("latest/{base}")
    fun getExchangeRates(
        @Path("base") base: String
    ): Call<ExchangeRateResponse>

    @GET("codes")
    fun getCodes(): Call<CodesResponse>
}