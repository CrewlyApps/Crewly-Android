package com.crewly.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.crewly.persistence.CrewlyDatabase
import com.crewly.utils.FileHelper
import com.crewly.utils.TimeDisplay
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Derek on 27/05/2018
 */
@Module
class AppModule {

  @Singleton
  @Provides
  fun provideContext(
    app: Application
  ): Context =
    app.applicationContext

  @Singleton
  @Provides
  fun provideCrewlyDatabase(app: Application): CrewlyDatabase = Room.databaseBuilder(app,
    CrewlyDatabase::class.java, "Crewly Database").build()

  @Singleton
  @Provides
  fun provideFirebaseAnalytics(
    context: Context
  ): FirebaseAnalytics =
    FirebaseAnalytics.getInstance(context)

  @Singleton
  @Provides
  fun provideCrashlytics(): FirebaseCrashlytics =
    FirebaseCrashlytics.getInstance()

  @Provides
  fun provideTimeDisplay(): TimeDisplay = TimeDisplay()

  @Provides
  fun provideFileHelper(
    androidFileHelper: AndroidFileHelper
  ): FileHelper =
    androidFileHelper
}