package com.crewly.logging

import android.util.Log
import com.crewly.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 28/07/2018
 */
@Singleton
class LoggingManager @Inject constructor(
  private val crashlyticsManager: CrashlyticsManager
) {

  /**
   * Log an exception to the console when in debug mode. The exception will also be uploaded to
   * a crash reporting service if [uploadError] is true.
   */
  fun logError(exception: Exception, uploadError: Boolean = true) {
    if (BuildConfig.DEBUG) exception.printStackTrace()
    if (uploadError) crashlyticsManager.logNonFatal(exception)
  }

  /**
   * Log an exception to the console when in debug mode. The exception will also be uploaded to
   * a crash reporting service if [uploadError] is true.
   */
  fun logError(throwable: Throwable, uploadError: Boolean = true) {
    if (BuildConfig.DEBUG) throwable.printStackTrace()
    if (uploadError) crashlyticsManager.logNonFatal(throwable)
  }

  fun logMessage(loggingFlow: LoggingFlow, message: String) {
    if (BuildConfig.DEBUG) Log.d(loggingFlow.loggingTag, message)
  }
}