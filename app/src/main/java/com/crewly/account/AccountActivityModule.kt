package com.crewly.account

import androidx.appcompat.app.AppCompatActivity
import com.crewly.activity.ActivityScope
import dagger.Binds
import dagger.Module

/**
 * Created by Derek on 17/06/2018
 */
@Module
abstract class AccountActivityModule {

  @Binds
  @ActivityScope
  abstract fun bindActivity(activity: AccountActivity): AppCompatActivity
}