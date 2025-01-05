package com.example.currency_converted.model

data class SymbolsResponse(
    val success: Boolean,
    val symbols: Map<String, String>

)