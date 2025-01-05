package com.example.currency_converted.model

import com.google.gson.annotations.SerializedName

data class CodesResponse(
    val result: String,
    val documentation: String,
    @SerializedName("terms_of_use")
    val termsOfUse: String,
    @SerializedName("supported_codes")
    val supportedCodes: List<List<String>>
)