package com.crewly.activity

import com.crewly.account.AccountFragmentModule
import com.crewly.auth.LoginActivity
import com.crewly.auth.LoginActivityModule
import com.crewly.logbook.LogbookFragmentModule
import com.crewly.roster.details.RosterDetailsActivity
import com.crewly.roster.details.RosterDetailsActivityModule
import com.crewly.roster.list.RosterListFragmentModule
import com.crewly.roster.raw.RawRosterActivity
import com.crewly.roster.raw.RawRosterActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ActivityModule {

  @ContributesAndroidInjector(modules = [
    HomeActivityModule::class,
    AccountFragmentModule::class,
    LogbookFragmentModule::class,
    RosterListFragmentModule::class
  ])
  @ActivityScope
  abstract fun bindHomeActivity(): HomeActivity

  @ContributesAndroidInjector(modules = [LoginActivityModule::class])
  @ActivityScope
  abstract fun bindLoginActivity(): LoginActivity

  @ContributesAndroidInjector(modules = [RawRosterActivityModule::class])
  @ActivityScope
  abstract fun bindRawRosterActivity(): RawRosterActivity

  @ContributesAndroidInjector(modules = [RosterDetailsActivityModule::class])
  @ActivityScope
  abstract fun bindRosterDetailsActivity(): RosterDetailsActivity
}