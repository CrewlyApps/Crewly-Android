package com.crewly.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module

/**
 * Created by Derek on 28/04/2019
 */
@Module
abstract class HomeActivityModule {

  @Binds
  @ActivityScope
  abstract fun bindActivity(activity: HomeActivity): AppCompatActivity
}