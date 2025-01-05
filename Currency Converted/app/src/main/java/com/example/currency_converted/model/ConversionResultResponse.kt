package com.example.currency_converted.model

data class Query(
    val from: String?,
    val to: String?,
    val amount: Double?
)

data class Info(
    val timestamp: Long?,
    val rate: Double?
)

data class ConversionResultResponse(
    val success: Boolean,
    val query: Query?,
    val info: Info?,
    val historical: String?,
    val date: String?,
    val result: Double?,
    val error: ErrorResponse?  // Add an error field if applicable
)

data class ErrorResponse(
    val code: String?,
    val info: String?
)

