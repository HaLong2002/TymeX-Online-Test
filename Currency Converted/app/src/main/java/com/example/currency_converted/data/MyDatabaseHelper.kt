package com.example.currency_converted.data.remote

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.currency_converted.model.ConversionRates
import com.example.currency_converted.model.ConversionRatesValue
import com.example.currency_converted.model.SupportedCodes
import com.example.currency_converted.util.const_variables


class MyDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "currency.db"
        private const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_CONVERSION_RATES = "conversion_rates"
        const val TABLE_SUPPORTED_CODES = "supported_codes"

        // Conversion rates table columns
        const val COLUMN_BASE_CODE = "base_code"
        const val COLUMN_TARGET_CODE = "target_code"
        const val COLUMN_RATE = "rate"
        const val COLUMN_LAST_UPDATED = "last_updated"

        // Supported codes table columns
        const val COLUMN_CODE = "code"
        const val COLUMN_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create conversion_rates table
        val createConversionRatesTable = """
            CREATE TABLE $TABLE_CONVERSION_RATES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_BASE_CODE TEXT,
                $COLUMN_TARGET_CODE TEXT,
                $COLUMN_RATE REAL,
                $COLUMN_LAST_UPDATED TEXT
            )
        """.trimIndent()

        // Create supported_codes table
        val createSupportedCodesTable = """
            CREATE TABLE $TABLE_SUPPORTED_CODES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODE TEXT,
                $COLUMN_NAME TEXT
            )
        """.trimIndent()

        db?.execSQL(createConversionRatesTable)
        db?.execSQL(createSupportedCodesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        // Drop tables if database version is updated
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONVERSION_RATES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUPPORTED_CODES")
        onCreate(db)
    }

    fun addConversionRates(rate: ConversionRates) {
        val db = this.writableDatabase

        try {
            rate.conversion_rates.forEach { (targetCode, conversionRate) ->
                val cv = ContentValues().apply {
                    put(COLUMN_BASE_CODE, rate.base_code)
                    put(COLUMN_TARGET_CODE, targetCode)
                    put(COLUMN_RATE, conversionRate)
                    put(COLUMN_LAST_UPDATED, rate.time_last_update_utc)
                }
                db.insert(TABLE_CONVERSION_RATES, null, cv)
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding conversion rates: ${e.message}")
        } finally {
            db?.close()
        }
    }

    fun addSupportedCodes(codes: SupportedCodes) {
        val db = this.writableDatabase

        try {
            codes.supportedCodes.forEach { (code, name) ->
                val cv = ContentValues().apply {
                    put(COLUMN_CODE, code)
                    put(COLUMN_NAME, name)
                }
                db.insert(TABLE_SUPPORTED_CODES, null, cv)
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding supported codes: ${e.message}")
        } finally {
            db?.close()
        }
    }

    fun clearAllConversionRates() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_CONVERSION_RATES")
    }

    fun getAllSupportedCodes(): List<String> {
        val listCodes: List<String> = const_variables().listCodes
        val codes = mutableListOf<String>()
        val db = this.readableDatabase
        //val query = "SELECT * FROM $TABLE_SUPPORTED_CODES"
        val query =
            "SELECT * FROM $TABLE_SUPPORTED_CODES WHERE code IN (${listCodes.joinToString(",") { "'$it'" }})"

        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }

        cursor?.use {
            while (it.moveToNext()) {
                codes.add(it.getString(it.getColumnIndexOrThrow(COLUMN_CODE)))
            }
        }
        Log.i("MyDatabaseHelper", codes.toString())
        return codes
    }

    fun getAllSupportedCodesAndName(): List<String> {
        val codes = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_SUPPORTED_CODES"

        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }

        cursor?.use {
            while (it.moveToNext()) {
                codes.add(
                    it.getString(it.getColumnIndexOrThrow(COLUMN_CODE))
                )
            }
        }
        return codes
    }

    fun getAllConversionRates(): List<ConversionRatesValue> {
        val rates = mutableListOf<ConversionRatesValue>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_CONVERSION_RATES"

        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }

        cursor?.use {
            while (it.moveToNext()) {
                val baseCurrency = it.getString(it.getColumnIndexOrThrow(COLUMN_BASE_CODE))
                val targetCurrency = it.getString(it.getColumnIndexOrThrow(COLUMN_TARGET_CODE))
                val rate = it.getDouble(it.getColumnIndexOrThrow(COLUMN_RATE))
                val lastUpdated = it.getString(it.getColumnIndexOrThrow(COLUMN_LAST_UPDATED))

                val conversionRate =
                    ConversionRatesValue(baseCurrency, targetCurrency, rate, lastUpdated)
                rates.add(conversionRate)
            }
        }
        Log.i("MyDatabaseHelper", "Conversion Rates: $rates")
        return rates
    }
}
