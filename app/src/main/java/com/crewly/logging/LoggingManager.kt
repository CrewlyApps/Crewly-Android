package com.crewly.logging

import com.crewly.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 28/07/2018
 */
@Singleton
class LoggingManager @Inject constructor(private val crashlyticsManager: CrashlyticsManager) {

    /**
     * Log an exception to the console when in debug mode. The exception will also be uploaded to
     * a crash reporting service if [uploadError] is true.
     */
    fun logError(exc: Exception, uploadError: Boolean = true) {
        if (BuildConfig.DEBUG) { exc.printStackTrace() }
        if (uploadError) { crashlyticsManager.logNonFatal(exc) }
    }
}