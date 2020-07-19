package com.crewly.persistence.preferences

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
    private const val VIEWED_FIRST_RYANAIR_ROSTER_FETCH_MESSAGE = "ViewedFirstRyanairRosterFetchMessage"
    private const val LAST_FETCHED_ROSTER_DATE_TIMESTAMP = "LastFetchedRosterDateTimestamp"
  }

  private val preferences = app.getSharedPreferences(NAME, Context.MODE_PRIVATE)
  private val editor = preferences.edit()

  fun saveCurrentAccount(
    crewCode: String
  ) {
    saveString(CURRENT_ACCOUNT_KEY, crewCode)
  }

  fun getCurrentAccount(): String = retrieveString(CURRENT_ACCOUNT_KEY)
  fun clearAccount() = deleteValue(CURRENT_ACCOUNT_KEY)

  fun saveAirportDataCopied() {
    saveBoolean(AIRPORT_DATA_COPIED_KEY, true)
  }

  fun getAirportDataCopied(): Boolean = retrieveBoolean(AIRPORT_DATA_COPIED_KEY)

  fun saveViewedFirstRyanairRosterFetchMessage() {
    saveBoolean(VIEWED_FIRST_RYANAIR_ROSTER_FETCH_MESSAGE, true)
  }

  fun getViewedFirstRyanairRosterFetchMessage() =
    retrieveBoolean(VIEWED_FIRST_RYANAIR_ROSTER_FETCH_MESSAGE)

  fun saveLastFetchedRosterDate(
    timestamp: Long
  ) {
    saveLong(
      key = LAST_FETCHED_ROSTER_DATE_TIMESTAMP,
      value = timestamp
    )
  }

  fun getLastFetchedRosterDate() =
    retrieveLong(LAST_FETCHED_ROSTER_DATE_TIMESTAMP)

  private fun saveString(
    key: String,
    value: String
  ) {
    synchronized(this) {
      editor.run {
        putString(key, value)
        apply()
      }
    }
  }

  private fun saveBoolean(
    key: String,
    value: Boolean
  ) {
    synchronized(this) {
      editor.run {
        putBoolean(key, value)
        apply()
      }
    }
  }

  private fun saveLong(
    key: String,
    value: Long
  ) {
    synchronized(this) {
      editor.run {
        putLong(key, value)
        apply()
      }
    }
  }

  private fun retrieveString(
    key: String
  ): String =
    synchronized(this) {
      return preferences.getString(key, "") ?: ""
    }

  private fun retrieveBoolean(
    key: String
  ): Boolean =
    synchronized(this) {
      return preferences.getBoolean(key, false)
    }

  private fun retrieveLong(
    key: String
  ): Long =
    synchronized(this) {
      return preferences.getLong(key, 0L)
    }

  private fun deleteValue(
    key: String
  ) {
    synchronized(this) {
      editor.run {
        remove(key)
        apply()
      }
    }
  }
}