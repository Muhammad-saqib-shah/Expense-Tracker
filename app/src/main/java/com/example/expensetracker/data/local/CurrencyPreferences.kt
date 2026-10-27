package com.example.expensetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "currency_preferences")

class CurrencyPreferences(private val context: Context) {
    companion object {
        private val KEY_CURRENCY_CODE = stringPreferencesKey("currency_code")
        private val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
    }

    val currencyCodeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_CURRENCY_CODE] ?: "USD" }

    val currencySymbolFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_CURRENCY_SYMBOL] ?: "$" }

    suspend fun saveCurrency(code: String, symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY_CODE] = code
            preferences[KEY_CURRENCY_SYMBOL] = symbol
        }
    }
}
