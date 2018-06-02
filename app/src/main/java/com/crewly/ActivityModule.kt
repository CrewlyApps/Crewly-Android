package com.crewly

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ActivityModule {

    @ContributesAndroidInjector()
    abstract fun bindMainActivity(): MainActivity
}