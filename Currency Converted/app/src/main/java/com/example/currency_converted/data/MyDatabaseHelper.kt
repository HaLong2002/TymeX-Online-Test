package com.example.currency_converted.data.remote

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.currency_converted.model.ConversionRates
import com.example.currency_converted.model.SupportedCodes


abstract class MyDatabaseHelper(context: Context) :
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

        rate.conversion_rates.forEach { (targetCode, conversionRate) ->
            val cv = ContentValues().apply {
                put(COLUMN_BASE_CODE, rate.base_code)
                put(COLUMN_TARGET_CODE, targetCode)
                put(COLUMN_RATE, conversionRate)
                put(COLUMN_LAST_UPDATED, rate.time_last_update_utc)
            }
            db.insert(TABLE_CONVERSION_RATES, null, cv)
        }

        db.close()
    }

    fun addSupportedCodes(codes: SupportedCodes) {
        val db = this.writableDatabase

        codes.supportedCodes.forEach { (code, name) ->
            val cv = ContentValues().apply {
                put(COLUMN_CODE, code)
                put(COLUMN_NAME, name)
            }
            db.insert(TABLE_SUPPORTED_CODES, null, cv)
        }
        db.close()
    }
}
