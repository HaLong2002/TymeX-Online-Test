package com.example.currency_converted

import android.content.ContentValues
import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.currency_converted.data.remote.remote.APIService
import com.example.currency_converted.data.remote.remote.ApiUtils
import com.example.currency_converted.model.SupportedCodes
import com.example.currency_converted.model.ConversionRates
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class MainActivity : AppCompatActivity() {

    private lateinit var input_amount: EditText
    private lateinit var input_converted: EditText
    private lateinit var base_currency: MaterialAutoCompleteTextView
    private lateinit var target_currency: MaterialAutoCompleteTextView
    private lateinit var exchange_rate: TextView
    private lateinit var swap_button: FloatingActionButton
    private lateinit var progress: CircularProgressIndicator
    private lateinit var converter_layout: LinearLayout
    private lateinit var exchange_rate_layout: RelativeLayout
    private lateinit var lb_error: TextView
    private lateinit var retry_button: Button
    private lateinit var mAPIService: APIService
    private var rates: Map<String, Double> = emptyMap()
    private lateinit var base_adapter: ArrayAdapter<String>
    private lateinit var target_adapter: ArrayAdapter<String>

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
        progress = findViewById(R.id.progress_circular)
        converter_layout = findViewById(R.id.converter_layout)
        exchange_rate_layout = findViewById(R.id.exchange_rate_layout)
        lb_error = findViewById(R.id.error)
        retry_button = findViewById(R.id.retry_button)

        rootLayout.setOnTouchListener { v, event ->
            if (currentFocus != null) {
                val rect = Rect()
                currentFocus!!.getGlobalVisibleRect(rect)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    currentFocus!!.clearFocus() // Remove focus from AutoCompleteTextView
                }
            }
            false
        }

        mAPIService = ApiUtils.getApiService();

        checkNetworkConnection()

        base_currency.setOnClickListener { base_currency.isCursorVisible = true }
        base_currency.setOnItemClickListener { parent, view, position, id ->
            hideCursorAndKeyboard(base_currency)
            // Get the selected item
            val selectedCurrency =
                parent.getItemAtPosition(position).toString().split("-")[0].trim()
            base_currency.setText(selectedCurrency)
            val target = target_currency.text.toString()
            //getExchangeRates(selectedCurrency, target)
        }

        target_currency.setOnClickListener { target_currency.isCursorVisible = true }
        target_currency.setOnItemClickListener { parent, view, position, id ->
            hideCursorAndKeyboard(target_currency)
            // Get the selected item
            val selectedCurrency =
                parent.getItemAtPosition(position).toString().split("-")[0].trim()
            target_currency.setText(selectedCurrency)
            val base = base_currency.text.toString()
            //getExchangeRates(base, selectedCurrency)
        }

        input_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.isNullOrEmpty())
                    input_converted.setText("")
                val userInput = p0.toString().trim().toDoubleOrNull()
                if (userInput != null) {
                    val base = base_currency.text.toString()
                    val target = target_currency.text.toString()
                    Log.d("base and targer currency", "$base $target")
                    convertExchangeRate(userInput, target)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        swap_button.setOnClickListener(View.OnClickListener {
            swap_Currency()
        })
    }

    fun hideCursorAndKeyboard(dropDown: AutoCompleteTextView) {
        dropDown.isCursorVisible = false
        // Hide the keyboard
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(dropDown.windowToken, 0)
    }

    fun checkNetworkConnection() {
        progress.visibility = View.VISIBLE
        lb_error.setText("")
        retry_button.visibility = View.GONE
        // Check the status of the network connection.
        val connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        // If the network is active
        if (networkInfo != null && networkInfo.isConnected) {
            getCurrencyCodes()
        } else {
            progress.visibility = View.GONE
            lb_error.setText(getString(R.string.no_network))
            retry_button.visibility = View.VISIBLE
        }
    }

    private fun getCurrencyCodes() {
        mAPIService.getCodes()
            .enqueue(object : retrofit2.Callback<SupportedCodes> {
                override fun onResponse(
                    call: retrofit2.Call<SupportedCodes>,
                    response: retrofit2.Response<SupportedCodes>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val currencyCodes: List<String> =
                                body.supportedCodes.map { "${it[0]} - ${it[1]}" }
                            showCurrencyCodes(currencyCodes)
                            Log.i("Codes:", "$body")
                            Log.i("Drop Down:", "$currencyCodes")
                            progress.visibility = View.GONE
                            converter_layout.visibility = View.VISIBLE
                            exchange_rate_layout.visibility = View.VISIBLE
                            lb_error.visibility = View.GONE
                            retry_button.visibility = View.GONE
                        } else {
                            Log.e("Error", "Response body is null")
                            lb_error.setText(getString(R.string.no_data))
                            progress.visibility = View.GONE
                            retry_button.visibility = View.GONE
                        }
                    } else {
                        Log.e("Error", "Error code: ${response.code()}")
                        lb_error.setText("Error code: ${response.code()}. Please try again later.")
                        progress.visibility = View.GONE
                        retry_button.visibility = View.GONE
                    }
                }

                override fun onFailure(call: retrofit2.Call<SupportedCodes>, t: Throwable) {
                    Log.e("Error", "Failed to fetch data: ${t.message}")
                    lb_error.setText("Failed to fetch data: ${t.message}. Please try again later.")
                    progress.visibility = View.GONE
                    retry_button.visibility = View.GONE
                }
            })
    }

    private fun showCurrencyCodes(currencyCodes: List<String>) {
        base_adapter = ArrayAdapter(this@MainActivity, R.layout.list_item, currencyCodes)
        base_currency.setAdapter(base_adapter)

        target_adapter = ArrayAdapter(this@MainActivity, R.layout.list_item, currencyCodes)
        target_currency.setAdapter(target_adapter)

        if (base_adapter.count > 0 && target_adapter.count > 0) {
            base_currency.setText(base_adapter.getItem(0).toString().split("-")[0].trim(), false)
            target_currency.setText(
                target_adapter.getItem(0).toString().split("-")[0].trim(),
                false
            )
//            getExchangeRates(
//                base_adapter.getItem(0).toString().split("-")[0].trim(),
//                target_adapter.getItem(0).toString().split("-")[0].trim()
//            )
        }
    }

    private fun getExchangeRates(base: String, target: String) {
        mAPIService.getExchangeRates(base)
            .enqueue(object : retrofit2.Callback<ConversionRates> {
                override fun onResponse(
                    call: retrofit2.Call<ConversionRates>,
                    response: retrofit2.Response<ConversionRates>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Log.i("Exchange rates: ", "$body")
                            rates = body.conversion_rates
                            val formatted = rates[target]?.toBigDecimal()?.toPlainString()
                            exchange_rate.setText("1 ${base} = ${formatted} ${target}")
                        } else {
                            println("Response body is null")
                        }
                    } else {
                        Log.e("Error", "${response.code()}")
                        Log.d("URL", call.request().url().toString())
                    }
                }

                override fun onFailure(call: retrofit2.Call<ConversionRates>, t: Throwable) {
                    Log.i(ContentValues.TAG, "Failed to fetch data: ${t.message}")
                }
            })
    }

    private fun convertExchangeRate(amount: Double, target: String) {
        val result: Double = amount * rates[target]!!
        val formatted = result.toBigDecimal().toPlainString()
        input_converted.setText(formatted)
    }

    private fun swap_Currency() {
        val base = base_currency.text.toString()
        val target = target_currency.text.toString()
        val amount = input_converted.text.toString()
        var amount_double = 0.0
        if (!amount.isEmpty())
            amount_double = amount.toDouble()

        // Swap the values
        base_currency.setAdapter(target_adapter)
        base_currency.setText(target, true)

        target_currency.setAdapter(base_adapter)
        target_currency.setText(base, true)

        input_amount.setText(amount)
        convertExchangeRate(amount_double, target)
    }

    fun reconnectNetwork(view: View) {
        checkNetworkConnection()
    }
}