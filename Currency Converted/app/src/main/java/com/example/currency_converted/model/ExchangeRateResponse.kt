package com.example.currency_converted.model

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(

    @SerializedName("success") var success: Boolean?,
    @SerializedName("timestamp") var timestamp: Int?,
    @SerializedName("base") var base: String?,
    @SerializedName("date") var date: String?,
    @SerializedName("rates") var rates: Map<String, Double>?

)