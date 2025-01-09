package com.example.currency_converted.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.currency_converted.R
import com.example.currency_converted.data.remote.MyDatabaseHelper
import com.example.currency_converted.model.ConversionRatesValue
import com.example.currency_converted.util.PreferencesHelper
import com.example.currency_converted.worker.InitialSetupWorker
import com.example.currency_converted.worker.UpdateConversionRatesWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.TimeUnit


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
    private lateinit var lbTimeLastUpdate: TextView

    private var supportedCodes: List<String> = emptyList()
    private var rates: List<ConversionRatesValue> = emptyList()

    private lateinit var base_adapter: ArrayAdapter<String>
    private lateinit var target_adapter: ArrayAdapter<String>
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var dbHelper: MyDatabaseHelper

    // Set default values for base and target currencies
    private val defaultBaseCurrency = "VND"
    private val defaultTargetCurrency = "USD"

    private val decimalFormat1 = DecimalFormat("#.##", DecimalFormatSymbols(Locale.FRANCE))
    private val decimalFormat2 = DecimalFormat("#.######", DecimalFormatSymbols(Locale.FRANCE))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        initializeApp()

        base_currency.setOnItemClickListener { parent, view, position, id ->
            //val selectedCurrency = parent.getItemAtPosition(position).toString().split("-")[0].trim()
            val selectedCurrency = parent.getItemAtPosition(position).toString().trim()
            base_currency.setText(selectedCurrency)
            val target = target_currency.text.toString()
            showIndicativeExchangeRate(selectedCurrency, target)
            if (input_amount.text.toString().isNotEmpty() && input_amount.text.toString()
                    .substringAfter(",").isNotEmpty()
            )
                convertExchangeRate(
                    input_amount.text.toString().replace(",", ".").toDouble(),
                    selectedCurrency,
                    target
                )
            else if (input_amount.text.toString().isNotEmpty() && input_amount.text.toString()
                    .substringAfter(",").isEmpty()
            )
                convertExchangeRate(
                    input_amount.text.toString().replace(",", "").toDouble(),
                    selectedCurrency,
                    target
                )
        }

        target_currency.setOnItemClickListener { parent, view, position, id ->
            //val selectedItem = parent.getItemAtPosition(position).toString().split("-")[0].trim()
            val selectedItem = parent.getItemAtPosition(position).toString().trim()
            val base = base_currency.text.toString()
            showIndicativeExchangeRate(base, selectedItem)
            if (input_amount.text.toString().isNotEmpty() && input_amount.text.toString()
                    .substringAfter(",").isNotEmpty()
            )
                convertExchangeRate(
                    input_amount.text.toString().replace(",", ".").toDouble(),
                    base,
                    selectedItem
                )
            else if (input_amount.text.toString().isNotEmpty() && input_amount.text.toString()
                    .substringAfter(",").isEmpty()
            )
                convertExchangeRate(
                    input_amount.text.toString().replace(",", "").toDouble(),
                    base,
                    selectedItem
                )
        }

        input_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.isNullOrEmpty())
                    input_converted.setText("0")
                val userInput = p0.toString().trim().toDoubleOrNull()
                if (userInput != null) {
                    val base = base_currency.text.toString()
                    val target = target_currency.text.toString()
                    Log.d("MainActivity", "$base $target")
                    convertExchangeRate(userInput, base, target)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val input = it.toString()
                    // Count the commas
                    val commaCount = input.count { char -> char == ',' }
                    if (commaCount > 1 || input.equals(",")) {
                        // Remove the last character if it's a comma
                        input_amount.setText(input.dropLast(1))
                        input_amount.setSelection(input_amount.text.length) // Move cursor to the end
                    }
                }
            }
        })
        input_amount.setOnClickListener { input_amount.isCursorVisible = true }

        swap_button.setOnClickListener { swapCurrencies() }
    }

    private fun initViews() {
        base_currency = findViewById(R.id.dropdown_menu1)
        target_currency = findViewById(R.id.dropdown_menu2)
        input_amount = findViewById(R.id.input_amount)
        //input_amount.keyListener = DigitsKeyListener.getInstance("0123456789,");
        input_converted = findViewById(R.id.input_converted)
        exchange_rate = findViewById(R.id.exchange_rate)
        swap_button = findViewById(R.id.swap_button)
        progress = findViewById(R.id.progress_circular)
        converter_layout = findViewById(R.id.converter_layout)
        exchange_rate_layout = findViewById(R.id.exchange_rate_layout)
        lb_error = findViewById(R.id.error)
        retry_button = findViewById(R.id.retry_button)
        lbTimeLastUpdate = findViewById(R.id.time_last_update)
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                // 1. Initial Setup
                prefsHelper = PreferencesHelper(applicationContext)
                dbHelper = MyDatabaseHelper(applicationContext)

                if (prefsHelper.isFirstLaunch()) {
                    Log.d("MainActivity", "First launch - fetching initial data")
                    // Wait for initial fetch to complete
                    withContext(Dispatchers.IO) {
                        setupInitialFetch()
                    }
                    prefsHelper.setFirstLaunchComplete()
                }

                // 2. Setup periodic updates
                setupPeriodicUpdate()

                // 3. Load all required data
                withContext(Dispatchers.IO) {
                    supportedCodes = getAllSupportedCodes()
                    rates = getAllConversionRates()
                }

                // 4. Verify data is loaded
                if (rates.isEmpty() || supportedCodes.isEmpty()) {
                    Log.d("MainActivity", "No data found, triggering fetch")
                    withContext(Dispatchers.IO) {
                        setupInitialFetch()
                        supportedCodes = getAllSupportedCodes()
                        rates = getAllConversionRates()
                    }
                }

                // 5. Now that all data is loaded, setup UI
                initViews()
                showSupportedCodes(supportedCodes)
                showIndicativeExchangeRate(defaultBaseCurrency, defaultTargetCurrency)
                showTimeLastUpdate(defaultBaseCurrency, defaultTargetCurrency)
            } catch (e: Exception) {
                Log.e("initializeApp", "Error: ${e.message}")
                Log.e("initializeApp", "Failed to initialize app")
            }
        }
    }

    private fun setupInitialFetch() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val initialWork = OneTimeWorkRequestBuilder<InitialSetupWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "initial_setup",
                ExistingWorkPolicy.KEEP,
                initialWork
            )
    }

    private fun setupPeriodicUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // Require internet connection
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<UpdateConversionRatesWorker>(
            24, TimeUnit.HOURS,  // Repeat every 24 hours
            15, TimeUnit.MINUTES // Flex period - work can start up to 15 minutes early
        )
            .setConstraints(constraints)
            .addTag("exchange_rate_update")
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "exchange_rate_update",
                ExistingPeriodicWorkPolicy.KEEP,  // Keep existing if one exists
                updateRequest
            )
    }

    private fun getAllSupportedCodes(): List<String> {
        //showSupportedCodes(dbHelper.getAllSupportedCodes())
        return dbHelper.getAllSupportedCodes()
    }

    private fun showSupportedCodes(list: List<String>) {
        base_adapter = object : ArrayAdapter<String>(this@MainActivity, R.layout.list_item, list) {
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val results = FilterResults()
                        results.values = list
                        results.count = list.size
                        return results
                    }

                    override fun publishResults(
                        constraint: CharSequence?,
                        results: FilterResults?
                    ) {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        base_currency.setAdapter(base_adapter)

        target_adapter =
            object : ArrayAdapter<String>(this@MainActivity, R.layout.list_item, list) {
                override fun getFilter(): Filter {
                    return object : Filter() {
                        override fun performFiltering(constraint: CharSequence?): FilterResults {
                            val results = FilterResults()
                            results.values = list
                            results.count = list.size
                            return results
                        }

                        override fun publishResults(
                            constraint: CharSequence?,
                            results: FilterResults?
                        ) {
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        target_currency.setAdapter(target_adapter)

        // Set default values
        base_currency.setText(defaultBaseCurrency, false)
        target_currency.setText(defaultTargetCurrency, false)
    }

    private fun getAllConversionRates(): List<ConversionRatesValue> {
        return dbHelper.getAllConversionRates()
    }

    private fun showIndicativeExchangeRate(baseCode: String, targetCode: String) {
        val rate =
            rates.find { it.base_code == baseCode && it.target_code == targetCode }?.rate?.toDouble()
        val formatted = decimalFormat2.format(rate?.let { BigDecimal(it) })
        exchange_rate.setText("1 $baseCode = $formatted $targetCode")
    }

    private fun showTimeLastUpdate(baseCode: String, targetCode: String) {
        val time =
            rates.find { it.base_code == baseCode && it.target_code == targetCode }?.last_updated
        val text = lbTimeLastUpdate.text.toString()
        lbTimeLastUpdate.setText(text + time)
    }

    private fun convertExchangeRate(amount: Double, baseCode: String, targetCode: String) {
        val rate =
            rates.find { it.base_code == baseCode && it.target_code == targetCode }?.rate?.toDouble()
        if (rate != null) {
            val result: Double = amount * rate
            var formatted = decimalFormat1.format(result)
            if (result < 1)
                formatted = decimalFormat2.format(BigDecimal(result))
            input_converted.setText(formatted)
        } else {
            input_converted.setText(R.string.no_data)
        }
    }

    private fun swapCurrencies() {
        val baseCode = base_currency.text.toString()
        val targetCode = target_currency.text.toString()

        // Swap the currency codes
        base_currency.setAdapter(target_adapter)
        base_currency.setText(targetCode, false)
        target_currency.setAdapter(base_adapter)
        target_currency.setText(baseCode, false)

        showIndicativeExchangeRate(targetCode, baseCode)

        // Swap amount input
        val amount = input_converted.text.toString()
        input_amount.setText(amount)
        input_amount.isCursorVisible = false

        // Convert amount
        var amount_target = 0.0
        if (amount.isNotEmpty()) {
            amount_target = amount.replace(",", ".").toDouble()
        }
        convertExchangeRate(amount_target, baseCode, targetCode)
    }

}