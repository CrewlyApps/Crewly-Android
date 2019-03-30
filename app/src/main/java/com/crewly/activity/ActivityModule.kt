package com.crewly.activity

import com.crewly.account.AccountActivity
import com.crewly.account.AccountActivityModule
import com.crewly.auth.LoginActivity
import com.crewly.auth.LoginActivityModule
import com.crewly.logbook.LogbookActivity
import com.crewly.logbook.LogbookActivityModule
import com.crewly.roster.details.RosterDetailsActivity
import com.crewly.roster.details.RosterDetailsActivityModule
import com.crewly.roster.list.RosterListActivity
import com.crewly.roster.list.RosterListActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ActivityModule {

  @ContributesAndroidInjector(modules = [AccountActivityModule::class])
  @ActivityScope
  abstract fun bindAccountActivity(): AccountActivity

  @ContributesAndroidInjector(modules = [LogbookActivityModule::class])
  @ActivityScope
  abstract fun bindLogbookActivity(): LogbookActivity

  @ContributesAndroidInjector(modules = [LoginActivityModule::class])
  @ActivityScope
  abstract fun bindLoginActivity(): LoginActivity

  @ContributesAndroidInjector(modules = [RosterDetailsActivityModule::class])
  @ActivityScope
  abstract fun bindRosterDetailsActivity(): RosterDetailsActivity

  @ContributesAndroidInjector(modules = [RosterListActivityModule::class])
  @ActivityScope
  abstract fun bindRosterActivity(): RosterListActivity
}