package com.crewly.logging

import com.crashlytics.android.Crashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 28/07/2018
 */
@Singleton
class CrashlyticsManager @Inject constructor() {

    companion object {
        const val IS_PILOT_KEY = "isPilot"
    }

    private var crashlytics: Crashlytics? = null

    init {
        crashlytics = Crashlytics.getInstance()
    }

    fun logNonFatal(exc: Exception) {
        crashlytics?.core?.logException(exc)
    }

    fun logNonFatal(exc: Throwable) {
        crashlytics?.core?.logException(exc)
    }

    fun addLoggingKey(key: String, value: Boolean) {
        crashlytics?.core?.setBool(key, value)
    }
}