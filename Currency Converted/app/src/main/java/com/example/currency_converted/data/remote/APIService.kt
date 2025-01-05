package com.example.currency_converted.data.remote

import com.example.currency_converted.model.ConversionResultResponse
import com.example.currency_converted.model.ExchangeRateResponse
import com.example.currency_converted.model.SymbolsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("/latest?")
    fun getExchangeRates(
        @Query("access_key") access_key: String,
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): Call<ExchangeRateResponse>

    @GET("/symbols?")
    fun getSymbols(
        @Query("access_key") access_key: String
    ): Call<SymbolsResponse>

    @GET("/convert?")
    fun getConversionResult(
        @Query("access_key") access_key: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double
    ): Call<ConversionResultResponse>
}