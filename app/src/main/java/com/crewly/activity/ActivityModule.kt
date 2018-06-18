package com.crewly.activity

import com.crewly.account.AccountActivity
import com.crewly.account.AccountActivityModule
import com.crewly.auth.LoginActivity
import com.crewly.auth.LoginActivityModule
import com.crewly.roster.RosterActivity
import com.crewly.roster.RosterActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [RosterActivityModule::class])
    @ActivityScope
    abstract fun bindMainActivity(): RosterActivity

    @ContributesAndroidInjector(modules = [LoginActivityModule::class])
    @ActivityScope
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [AccountActivityModule::class])
    @ActivityScope
    abstract fun bindAccountActivity(): AccountActivity
}