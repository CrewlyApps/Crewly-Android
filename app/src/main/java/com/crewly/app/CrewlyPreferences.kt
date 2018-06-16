package com.crewly.app

import android.app.Application
import android.content.Context
import javax.inject.Inject

/**
 * Created by Derek on 16/06/2018
 */
class CrewlyPreferences @Inject constructor(app: Application) {

    companion object {
        private const val NAME = "CrewlyPreferences"

        private const val CURRENT_ACCOUNT_KEY = "CurrentAccount"
    }

    private val preferences = app.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    private val editor = preferences.edit()

    fun saveCurrentAccount(crewCode: String) { saveString(CURRENT_ACCOUNT_KEY, crewCode) }
    fun getCurrentAccount(): String = retrieveString(CURRENT_ACCOUNT_KEY)

    private fun saveString(key: String, value: String) {
        synchronized (this) {
            editor.putString(key, value)
            editor.apply()
        }
    }

    private fun retrieveString(key: String): String {
        synchronized (this) { return preferences.getString(key, "") }
    }
}