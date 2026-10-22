package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApiKeyManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("budget_bites_api_prefs", Context.MODE_PRIVATE)

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    fun getApiKey(): String {
        return prefs.getString(KEY_GOOGLE_PLACES_API, "") ?: ""
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_GOOGLE_PLACES_API, key.trim()).apply()
        _apiKeyFlow.value = key.trim()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_GOOGLE_PLACES_API).apply()
        _apiKeyFlow.value = ""
    }

    fun hasApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    companion object {
        private const val KEY_GOOGLE_PLACES_API = "google_places_api_key"

        @Volatile
        private var INSTANCE: ApiKeyManager? = null

        fun getInstance(context: Context): ApiKeyManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ApiKeyManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
