package com.example.currency_converted

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.currency_converted.data.remote.APIService
import com.example.currency_converted.data.remote.ApiUtils
import com.example.currency_converted.model.ExchangeRateResponse
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var input_amount: EditText
    private lateinit var input_converted: EditText
    private lateinit var mAPIService: APIService
    private val API_KEY = "10dd2cc62813bd399efd60213d99fb9f"
    private var rates : Map<String, Double> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val from_currency: Spinner = findViewById(R.id.from_currency);
        val to_currency: Spinner = findViewById(R.id.to_currency);
        input_amount = findViewById(R.id.input_amount);
        input_converted = findViewById(R.id.input_converted);

        mAPIService = ApiUtils.getApiService();
        getExchangeRates()
    }

    fun getExchangeRates() {
        mAPIService.getExchangeRates(API_KEY)
            .enqueue(object : retrofit2.Callback<ExchangeRateResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ExchangeRateResponse>,
                    response: retrofit2.Response<ExchangeRateResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Handle the response
                            val responseBody : String = body.toString()
                            showResponse(responseBody)
                            println("Exchange rates: $body")
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

    fun showResponse(response : String) {
        try {
            // Convert the response into a JSON object.
            val jsonObject = JSONObject(response)

            // Get the JSONArray of book items.
            val itemsArray = jsonObject.getJSONArray("rates")

            // Initialize iterator and results fields.
            var i = 0
            var title: String? = null
            var authors: String? = null
            listBook = ArrayList<Array<String>>()

            // Look for results in the items array, exiting when both the title and author
            // are found or when all items have been checked.
            while (i < itemsArray.length() || authors == null && title == null) {
                // Get the current item information.
                val book = itemsArray.getJSONObject(i)
                val volumeInfo = book.getJSONObject("volumeInfo")

                // Try to get the author and title from the current item,
                // catch if either field is empty and move on.
                try {
                    title = volumeInfo.getString("title")
                    authors = volumeInfo.getString("authors")
                    val title_authors = arrayOf(title, authors)
                    listBook.add(title_authors)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Move to the next item.
                i++
            }

            // If both are found, display the result.
            //if (title != null && authors != null){
            if (listBook != null) {
                //mTitleText.setText(title);
                //mAuthorText.setText(authors);
                loading.setText("")
                mBookInput.setText("")

                // Hiện listView
                if (listbook.getVisibility() == View.GONE) listbook.setVisibility(View.VISIBLE)
                bookListViewAdapter = BookListViewAdapter(listBook)
                System.out.println(bookListViewAdapter.getCount())
                listbook.setAdapter(bookListViewAdapter)
                bookListViewAdapter.notifyDataSetChanged()
            } else {
                // If none are found, update the UI to show failed results.
                loading.setText(R.string.no_results)
                //mAuthorText.setText("");
            }
        } catch (e: Exception) {
            // If onPostExecute does not receive a proper JSON string, update the UI to show failed results.
            loading.setText(R.string.no_results)
            //mAuthorText.setText("");
            e.printStackTrace()
        }
    }
}