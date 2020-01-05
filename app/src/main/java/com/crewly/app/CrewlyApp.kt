package com.crewly.app

import android.annotation.SuppressLint
import android.app.Application
import com.crewly.BuildConfig
import com.crewly.account.AccountManager
import com.crewly.logging.LoggingManager
import com.crewly.persistence.preferences.CrewlyPreferences
import com.crewly.repositories.AirportsRepository
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
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
  @Inject lateinit var loggingManager: LoggingManager

  override fun onCreate() {
    super.onCreate()

    SoLoader.init(this, false)
    if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
      val client = AndroidFlipperClient.getInstance(this)
      client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
      client.addPlugin(DatabasesFlipperPlugin(this))
      client.start()
    }

    JodaTimeAndroid.init(this)
    DateTimeZone.setDefault(DateTimeZone.UTC)

    DaggerAppComponent
      .builder()
      .application(this)
      .build()
      .inject(this)

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