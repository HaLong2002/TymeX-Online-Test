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
import com.example.currency_converted.model.CodesResponse
import com.example.currency_converted.model.ExchangeRateResponse

class MainActivity : AppCompatActivity() {

    private lateinit var input_amount: EditText
    private lateinit var input_converted: EditText
    private lateinit var from_currency: Spinner
    private lateinit var to_currency: Spinner
    private lateinit var mAPIService: APIService

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

        getCodes()

        input_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.isNullOrEmpty())
                    input_converted.setText("")
                val userInput = p0.toString().trim().toDoubleOrNull()
                if (userInput != null) {
                    val from = from_currency.selectedItem.toString()
                    val to = to_currency.selectedItem.toString()
                    val base = from.split(",")[0].trim().replace("[", "").replace("]", "")
                    val symbol = to.split(",")[0].trim().replace("[", "").replace("]", "")
                    Log.d("from_currenty", from)
                    Log.d("to_currency", to)
                    getExchangeRates(base, symbol, userInput)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    private fun getCodes() {
        mAPIService.getCodes()
            .enqueue(object : retrofit2.Callback<CodesResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CodesResponse>,
                    response: retrofit2.Response<CodesResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Handle the response
                            val codes: List<List<String>> = body.supportedCodes
                            populateSpinner(from_currency, codes)
                            populateSpinner(to_currency, codes)
                            println("Codes: $body")
                        } else {
                            println("Response body is null")
                        }
                        Log.i(ContentValues.TAG, "Get data successfully" + response.code())
                    } else {
                        // Handle the error
                        Log.e("Error", "${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<CodesResponse>, t: Throwable) {
                    // Handle failure
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun getExchangeRates(base: String, symbol: String, amount: Double) {
        mAPIService.getExchangeRates(base)
            .enqueue(object : retrofit2.Callback<ExchangeRateResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ExchangeRateResponse>,
                    response: retrofit2.Response<ExchangeRateResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Handle the response
                            val rates: Map<String, Double> = body.conversion_rates
                            println("Exchange rates: $body")
                            val result: Double = amount * rates[symbol]!!
                            input_converted.setText(result.toString())
                        } else {
                            println("Response body is null")
                        }
                        Log.i(ContentValues.TAG, "Get data successfully" + response.code())
                    } else {
                        // Handle the error
                        Log.e("Error", "${response.code()}")
                        Log.d("URL", call.request().url().toString())
                    }
                }

                override fun onFailure(call: retrofit2.Call<ExchangeRateResponse>, t: Throwable) {
                    // Handle failure
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun populateSpinner(spinner: Spinner, data: List<List<String>>) {
        // Create an ArrayAdapter
        val adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_item,
            data
        )

        // Set the dropdown layout style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach the adapter to the Spinner
        spinner.adapter = adapter
    }

}