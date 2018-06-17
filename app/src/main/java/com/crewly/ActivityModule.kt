package com.crewly

import com.crewly.account.AccountActivity
import com.crewly.auth.LoginActivity
import com.crewly.roster.RosterActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun bindAccountActivity(): AccountActivity

    @ContributesAndroidInjector
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector()
    abstract fun bindMainActivity(): RosterActivity
}