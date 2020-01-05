package com.crewly.roster.raw

import androidx.appcompat.app.AppCompatActivity
import com.crewly.activity.ActivityScope
import dagger.Binds
import dagger.Module

@Module
abstract class RawRosterActivityModule {

  @Binds
  @ActivityScope
  abstract fun bindActivity(activity: RawRosterActivity): AppCompatActivity
}