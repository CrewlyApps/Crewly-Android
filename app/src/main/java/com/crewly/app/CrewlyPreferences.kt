package com.crewly.app

import android.app.Application
import android.content.Context
import javax.inject.Inject

/**
 * Created by Derek on 16/06/2018
 */
class CrewlyPreferences @Inject constructor(
  app: Application
) {

  companion object {
    private const val NAME = "CrewlyPreferences"

    private const val CURRENT_ACCOUNT_KEY = "CurrentAccount"
    private const val AIRPORT_DATA_COPIED_KEY = "AirportDataCopied"
  }

  private val preferences = app.getSharedPreferences(NAME, Context.MODE_PRIVATE)
  private val editor = preferences.edit()

  fun clearPreferences() {
    synchronized(this) { editor.clear().apply() }
  }

  fun saveCurrentAccount(crewCode: String) {
    saveString(CURRENT_ACCOUNT_KEY, crewCode)
  }

  fun getCurrentAccount(): String = retrieveString(CURRENT_ACCOUNT_KEY)
  fun deleteAccount() = deleteValue(CURRENT_ACCOUNT_KEY)

  fun saveAirportDataCopied() {
    saveBoolean(AIRPORT_DATA_COPIED_KEY, true)
  }

  fun getAirportDataCopied(): Boolean = retrieveBoolean(AIRPORT_DATA_COPIED_KEY)

  private fun saveString(key: String, value: String) {
    synchronized(this) {
      editor.putString(key, value)
      editor.apply()
    }
  }

  private fun saveBoolean(key: String, value: Boolean) {
    synchronized(this) {
      editor.putBoolean(key, value)
      editor.apply()
    }
  }

  private fun retrieveString(key: String): String {
    synchronized(this) { return preferences.getString(key, "") ?: "" }
  }

  private fun retrieveBoolean(key: String): Boolean {
    synchronized(this) { return preferences.getBoolean(key, false) }
  }

  private fun deleteValue(key: String) {
    synchronized(this) {
      editor.remove(key)
      editor.apply()
    }
  }
}