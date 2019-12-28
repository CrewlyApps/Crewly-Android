package com.crewly.app

import android.annotation.SuppressLint
import android.app.Application
import com.crewly.account.AccountManager
import com.crewly.aws.AwsManager
import com.crewly.repositories.AirportsRepository
import com.crewly.logging.LoggingManager
import com.crewly.persistence.preferences.CrewlyPreferences
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTimeZone
import javax.inject.Inject

/**
 * Created by Derek on 27/05/2018
 */
class CrewlyApp: Application(), HasAndroidInjector {

  @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

  @Inject lateinit var airportsRepository: AirportsRepository
  @Inject lateinit var crewlyPreferences: CrewlyPreferences
  @Inject lateinit var accountManager: AccountManager
  @Inject lateinit var awsManager: AwsManager
  @Inject lateinit var loggingManager: LoggingManager

  override fun onCreate() {
    super.onCreate()

    JodaTimeAndroid.init(this)
    DateTimeZone.setDefault(DateTimeZone.UTC)

    DaggerAppComponent
      .builder()
      .application(this)
      .build()
      .inject(this)

    awsManager.init()

    copyAirportDataIfNeeded()
  }

  override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

  @SuppressLint("CheckResult")
  private fun copyAirportDataIfNeeded() {
    if (!crewlyPreferences.getAirportDataCopied()) {
      airportsRepository
        .copyAirportsToDatabase()
        .subscribe({
          crewlyPreferences.saveAirportDataCopied()
        }) { loggingManager.logError(it) }
    }
  }
}