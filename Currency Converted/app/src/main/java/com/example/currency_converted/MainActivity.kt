package com.example.currency_converted

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.currency_converted.data.remote.APIService
import com.example.currency_converted.data.remote.ApiUtils
import com.example.currency_converted.model.CodesResponse
import com.example.currency_converted.model.ExchangeRateResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class MainActivity : AppCompatActivity() {

    private lateinit var input_amount: EditText
    private lateinit var input_converted: EditText
    private lateinit var base_currency: MaterialAutoCompleteTextView
    private lateinit var target_currency: MaterialAutoCompleteTextView
    private lateinit var exchange_rate: TextView
    private lateinit var swap_button: FloatingActionButton
    private lateinit var mAPIService: APIService
    private var rates: Map<String, Double> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        base_currency = findViewById(R.id.dropdown_menu1);
        target_currency = findViewById(R.id.dropdown_menu2);
        input_amount = findViewById(R.id.input_amount);
        input_converted = findViewById(R.id.input_converted);
        exchange_rate = findViewById(R.id.exchange_rate);
        val rootLayout = findViewById<ViewGroup>(R.id.main)
        swap_button = findViewById(R.id.swap_button)

//        rootLayout.setOnTouchListener { v, event ->
//            if (currentFocus != null) {
//                val rect = Rect()
//                currentFocus!!.getGlobalVisibleRect(rect)
//                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
//                    currentFocus!!.clearFocus() // Remove focus from AutoCompleteTextView
//                }
//            }
//            false
//        }

        mAPIService = ApiUtils.getApiService();

        getCurrencyCodes()

        // Show dropdown when clicked
        base_currency.setOnClickListener {base_currency.showDropDown()}
        base_currency.setOnItemClickListener { parent, view, position, id ->
            // Get the selected item
            val selectedCurrency = parent.getItemAtPosition(position).toString().split("-")[0].trim()
            base_currency.setText(selectedCurrency)
            val target = target_currency.text.toString()
            getExchangeRates(selectedCurrency, target)
        }

        target_currency.setOnClickListener {
            target_currency.showDropDown()
        }
        target_currency.setOnItemClickListener { parent, view, position, id ->
            // Get the selected item
            val selectedCurrency = parent.getItemAtPosition(position).toString().split("-")[0].trim()
            target_currency.setText(selectedCurrency)
            val base = base_currency.text.toString()
            getExchangeRates(base, selectedCurrency)
        }

        input_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.isNullOrEmpty())
                    input_converted.setText("")
                val userInput = p0.toString().trim().toDoubleOrNull()
                if (userInput != null) {
                    val base = base_currency.text.toString()
                    val target = target_currency.text.toString()
                    Log.d("base currency", base)
                    Log.d("target currency", target)
                    convertExchangeRate(userInput, target)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        swap_button.setOnClickListener(View.OnClickListener {
            swap_Currency()
        })
    }

    private fun getCurrencyCodes() {
        mAPIService.getCodes()
            .enqueue(object : retrofit2.Callback<CodesResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CodesResponse>,
                    response: retrofit2.Response<CodesResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val currencyCodes: List<String> = body.supportedCodes.map { "${it[0]} - ${it[1]}" }
                            showCurrencyCodes(currencyCodes)
                            Log.i("Codes:", "$body")
                            Log.i("Drop Down:", "$currencyCodes")
                        } else {
                            println("Response body is null")
                        }
                    } else {
                        Log.e("Error", "${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<CodesResponse>, t: Throwable) {
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun showCurrencyCodes(currencyCodes: List<String>) {
        val adapter1 = ArrayAdapter(this@MainActivity, R.layout.list_item, currencyCodes)
        base_currency.setAdapter(adapter1)
        val adapter2 = ArrayAdapter(this@MainActivity, R.layout.list_item, currencyCodes)
        target_currency.setAdapter(adapter2)

        if (adapter1.count > 0 && adapter2.count > 0) {
            base_currency.setText(adapter1.getItem(0).toString().split("-")[0].trim(), false)
            target_currency.setText(adapter2.getItem(0).toString().split("-")[0].trim(), false)
            getExchangeRates(adapter1.getItem(0).toString().split("-")[0].trim(),
                adapter2.getItem(0).toString().split("-")[0].trim())
        }
    }

    private fun getExchangeRates(base: String, target: String) {
        mAPIService.getExchangeRates(base)
            .enqueue(object : retrofit2.Callback<ExchangeRateResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ExchangeRateResponse>,
                    response: retrofit2.Response<ExchangeRateResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Log.i("Exchange rates: ", "$body")
                            rates = body.conversion_rates
                            exchange_rate.setText("1 ${base} = ${rates[target]} ${target}")
                        } else {
                            println("Response body is null")
                        }
                    } else {
                        Log.e("Error", "${response.code()}")
                        Log.d("URL", call.request().url().toString())
                    }
                }

                override fun onFailure(call: retrofit2.Call<ExchangeRateResponse>, t: Throwable) {
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun convertExchangeRate(amount: Double, target: String) {
        val result: Double = amount * rates[target]!!
        input_converted.setText(result.toString())
    }

    private fun swap_Currency() {

    }
}