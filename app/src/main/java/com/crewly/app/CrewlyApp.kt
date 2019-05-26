package com.crewly.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import com.crewly.account.AccountManager
import com.crewly.aws.AwsManager
import com.crewly.duty.AirportHelper
import com.crewly.logging.LoggingManager
import com.squareup.moshi.Moshi
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.reactivex.Scheduler
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTimeZone
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 27/05/2018
 */
class CrewlyApp: Application(), HasActivityInjector {

  @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

  @Inject lateinit var crewlyPreferences: CrewlyPreferences
  @Inject lateinit var crewlyDatabase: CrewlyDatabase
  @Inject lateinit var accountManager: AccountManager
  @Inject lateinit var awsManager: AwsManager
  @Inject lateinit var loggingManager: LoggingManager
  @Inject lateinit var moshi: Moshi
  @field: [Inject Named(RxModule.IO_THREAD)] lateinit var ioThread: Scheduler

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

  override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector

  @SuppressLint("CheckResult")
  private fun copyAirportDataIfNeeded() {
    if (!crewlyPreferences.getAirportDataCopied()) {
      val airportHelper = AirportHelper(
        context = this,
        crewlyDatabase = crewlyDatabase,
        moshi = moshi,
        ioThread = ioThread
      )

      airportHelper
        .copyAirportsToDatabase()
        .subscribe({
          crewlyPreferences.saveAirportDataCopied()
        }) { loggingManager.logError(it) }
    }
  }
}