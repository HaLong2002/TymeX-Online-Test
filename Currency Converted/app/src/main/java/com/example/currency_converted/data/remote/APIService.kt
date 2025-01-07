package com.example.currency_converted.data.remote.remote

import com.example.currency_converted.model.SupportedCodes
import com.example.currency_converted.model.ConversionRates
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {
    @GET("latest/{base}")
    fun getExchangeRates(
        @Path("base") base: String
    ): Call<ConversionRates>

    @GET("codes")
    fun getCodes(): Call<SupportedCodes>
}