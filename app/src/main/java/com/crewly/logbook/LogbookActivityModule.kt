package com.crewly.logbook

import androidx.appcompat.app.AppCompatActivity
import com.crewly.activity.ActivityScope
import dagger.Binds
import dagger.Module

/**
 * Created by Derek on 26/08/2018
 */
@Module
abstract class LogbookActivityModule {

  @Binds
  @ActivityScope
  abstract fun bindActivity(activity: LogbookActivity): AppCompatActivity
}