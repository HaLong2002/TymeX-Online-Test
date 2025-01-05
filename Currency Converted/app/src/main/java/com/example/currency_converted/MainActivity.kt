package com.example.currency_converted

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.currency_converted.data.remote.APIService
import com.example.currency_converted.data.remote.ApiUtils
import com.example.currency_converted.model.ConversionResultResponse
import com.example.currency_converted.model.ExchangeRateResponse
import com.example.currency_converted.model.SymbolsResponse

class MainActivity : AppCompatActivity() {

    private lateinit var input_amount: EditText
    private lateinit var input_converted: EditText
    private lateinit var from_currency: Spinner
    private lateinit var to_currency: Spinner
    private lateinit var mAPIService: APIService
    private val API_KEY = "10dd2cc62813bd399efd60213d99fb9f"
    private var rates: Map<String, Double> = emptyMap()
    private val symbols = mutableListOf<SymbolsResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        from_currency = findViewById(R.id.from_currency);
        to_currency = findViewById(R.id.to_currency);
        input_amount = findViewById(R.id.input_amount);
        input_converted = findViewById(R.id.input_converted);

        mAPIService = ApiUtils.getApiService();

        getSymbols()

        input_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val userInput = p0.toString().trim().toDoubleOrNull()
                if (userInput != null) {
                    val from = from_currency.selectedItem.toString()
                    val to = to_currency.selectedItem.toString()
                    Log.d("from_currenty", from)
                    Log.d("to_currency", to)
                    //getConversionResult(userInput, from, to)
                    getExchangeRate(from, to, userInput)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    private fun getSymbols() {
        mAPIService.getSymbols(API_KEY)
            .enqueue(object : retrofit2.Callback<SymbolsResponse> {
                override fun onResponse(
                    call: retrofit2.Call<SymbolsResponse>,
                    response: retrofit2.Response<SymbolsResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Handle the response
                            val symbols: Map<String, String> = body.symbols
                            populateSpinner(from_currency, symbols)
                            populateSpinner(to_currency, symbols)
                            println("Symbols: $symbols")
                        } else {
                            println("Response body is null")
                        }
                        Log.i(ContentValues.TAG, "Get data successfully" + response.code())
                    } else {
                        // Handle the error
                        Log.e("Error", "${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<SymbolsResponse>, t: Throwable) {
                    // Handle failure
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun getExchangeRate(base: String, symbol: String, amount: Double) {
        mAPIService.getExchangeRates(API_KEY, base, symbol)
            .enqueue(object : retrofit2.Callback<ExchangeRateResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ExchangeRateResponse>,
                    response: retrofit2.Response<ExchangeRateResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Handle the response
                            rates = body.rates!!
                            println("Exchange rates: $rates")
                            val result: Double = amount * rates[symbol]!!
                            input_converted.setText(result.toString())
                        } else {
                            println("Response body is null")
                        }
                        Log.i(ContentValues.TAG, "Get data successfully" + response.code())
                    } else {
                        // Handle the error
                        Log.e("Error", "${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<ExchangeRateResponse>, t: Throwable) {
                    // Handle failure
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun populateSpinner(spinner: Spinner, data: Map<String, String>) {
        // Extract only the keys (e.g., "VND")
        val items = data.keys.toList()

        // Create an ArrayAdapter
        val adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_item,
            items
        )

        // Set the dropdown layout style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach the adapter to the Spinner
        spinner.adapter = adapter
    }

    private fun getConversionResult(amount: Double, from: String, to: String) {
        mAPIService.getConversionResult(API_KEY, from, to, amount)
            .enqueue(object : retrofit2.Callback<ConversionResultResponse> {
                override fun onResponse(call: retrofit2.Call<ConversionResultResponse>,
                                        response: retrofit2.Response<ConversionResultResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Handle the response
                            val result = body.result.toString()
                            input_converted.setText(result)
                            //println("Conversion Result: from: ${body.query.from}, to: ${body.query.to}, amount: ${body.query.amount} ")
                        } else {
                            println("Response body is null")
                        }
                        Log.i(ContentValues.TAG, "Get data successfully " + response.code())
                        Log.i(ContentValues.TAG, "Raw response body: ${response.body()?.toString()}")
                    } else {
                        // Handle the error
                        Log.e("Error", "${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<ConversionResultResponse>, t: Throwable) {
                    // Handle failure
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

}