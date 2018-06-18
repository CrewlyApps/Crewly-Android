package com.crewly.roster

import android.app.Activity
import com.crewly.activity.ActivityScope
import dagger.Binds
import dagger.Module

/**
 * Created by Derek on 17/06/2018
 */
@Module
abstract class RosterActivityModule {

    @Binds
    @ActivityScope
    abstract fun bindActivity(activity: RosterActivity): Activity
}