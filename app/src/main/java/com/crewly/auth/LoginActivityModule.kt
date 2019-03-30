package com.crewly.auth

import android.support.v7.app.AppCompatActivity
import com.crewly.activity.ActivityScope
import dagger.Binds
import dagger.Module

/**
 * Created by Derek on 17/06/2018
 */
@Module
abstract class LoginActivityModule {

  @Binds
  @ActivityScope
  abstract fun bindActivity(activity: LoginActivity): AppCompatActivity
}