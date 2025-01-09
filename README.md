# Currency Converter App

A simple Android application that provides real-time currency conversion using **Exchange Rate API** (https://www.exchangerate-api.com/docs/overview). The app updates currency rates daily and stores them locally for offline access.

## Features

- Real-time currency conversion
- Daily rate updates
- Offline support with local database storage
- Support for 5 currencies: VND, USD, JPY, KRW, SGD

## Project Structure

**1. Data Layer**

- **APIService**: Defines API endpoints for currency data
- **ApiUtils**: Helper functions for API calls
- **RetrofitClient**: Network client configuration
- **MyDatabaseHelper**: SQLite database management

**2. Models**

- **ConversionRates**: Data model for currency conversion rates
- **SupportedCodes**: Data model for supported currency codes

**3. UI Layer**

- **MainActivity**: Main screen with conversion functionality

**4. Utilities**

- **const_variables**: Application constants and configuration
- **PreferencesHelper**: Local preferences management

**5. Workers**

- **InitialSetupWorker**: Handles first-time data setup
- **UpdateConversionRatesWorker**: Manages periodic rate updates

## Data Flow

**1. On first launch:**

- App fetches supported currencies
- Downloads initial conversion rates
- Stores data in local database

**2. Regular operation:**

- Updates rates daily
- Uses local database for conversions
- Maintains offline functionality

## Dependencies

    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.6.1")
    implementation ("androidx.work:work-runtime-ktx:2.9.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

## Setup

- Clone the repository
- Add your Exchange Rate API key in **const_variables.kt**
- Build and run the project

## Notes

**Initial App Launch:** When launching the app for the first time, it may take approximately 1 minute to fetch all the required data. After this, reopen the app to ensure everything is loaded correctly.

## Challenges Encountered

- **Delayed API Responses:** During the first app initialization, fetching a large dataset from the API caused delays. This was mitigated by implementing a CircularProgressIndicator to improve user feedback.
- **Error Handling:** Making sure the app works smoothly even when API requests fail. If fetching data from the API doesn’t work, the app uses the data already saved on the device instead.
- **UI Consistency Across Devices:** Ensuring the app layout fits various screen sizes required careful use of responsive design techniques.

## App Demo

Watch the video demo of the app:
https://youtu.be/-m4PZDtIPP4?si=A3A2RwZp-GFdsv02
