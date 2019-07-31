package com.crewly.app

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import javax.inject.Inject

/**
 * Created by Derek on 31/07/2019
 */
class CrewlyEncryptedPreferences @Inject constructor(
  app: Application
) {

  companion object {
    private const val NAME = "CrewlyEncryptedPreferences"
  }

  private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

  private val preferences = EncryptedSharedPreferences.create(
    NAME,
    masterKeyAlias,
    app,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  private val editor = preferences.edit()

  fun savePassword(
    crewCode: String,
    password: String
  ) {
    saveString(
      key = crewCode,
      value = password
    )
  }

  fun getPassword(
    crewCode: String
  ): String =
    retrieveString(crewCode)

  private fun saveString(key: String, value: String) {
    synchronized(this) {
      editor.putString(key, value)
      editor.apply()
    }
  }

  private fun retrieveString(key: String): String {
    synchronized(this) { return preferences.getString(key, "") ?: "" }
  }
}