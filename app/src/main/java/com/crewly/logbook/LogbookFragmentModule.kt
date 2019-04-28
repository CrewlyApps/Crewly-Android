package com.crewly.logbook

import com.crewly.activity.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 28/04/2019
 */
@Module
abstract class LogbookFragmentModule {

  @ContributesAndroidInjector
  @FragmentScope
  abstract fun contributeFragment(): LogbookFragment
}