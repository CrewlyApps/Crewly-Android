package com.crewly.roster.details

import android.app.Activity
import com.crewly.activity.ActivityScope
import dagger.Binds
import dagger.Module

/**
 * Created by Derek on 15/07/2018
 */
@Module
abstract class RosterDetailsActivityModule {

    @Binds
    @ActivityScope
    abstract fun bindActivity(activity: RosterDetailsActivity): Activity
}