package com.crewly.activity

import com.crewly.account.AccountActivity
import com.crewly.account.AccountActivityModule
import com.crewly.auth.LoginActivity
import com.crewly.auth.LoginActivityModule
import com.crewly.roster.list.RosterListActivity
import com.crewly.roster.list.RosterListActivityModule
import com.crewly.roster.details.RosterDetailsActivity
import com.crewly.roster.details.RosterDetailsActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [RosterListActivityModule::class])
    @ActivityScope
    abstract fun bindRosterActivity(): RosterListActivity

    @ContributesAndroidInjector(modules = [RosterDetailsActivityModule::class])
    @ActivityScope
    abstract fun bindRosterDetailsActivity(): RosterDetailsActivity

    @ContributesAndroidInjector(modules = [LoginActivityModule::class])
    @ActivityScope
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [AccountActivityModule::class])
    @ActivityScope
    abstract fun bindAccountActivity(): AccountActivity
}