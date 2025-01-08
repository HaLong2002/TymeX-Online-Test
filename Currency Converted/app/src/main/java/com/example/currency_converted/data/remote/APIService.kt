package com.example.currency_converted.data.remote.remote

import com.example.currency_converted.model.ConversionRates
import com.example.currency_converted.model.SupportedCodes
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {
    @GET("latest/{base}")
    suspend fun getConversionRates(
        @Path("base") base: String
    ): ConversionRates

    @GET("codes")
    suspend fun getSupportedCodes(): SupportedCodes
}