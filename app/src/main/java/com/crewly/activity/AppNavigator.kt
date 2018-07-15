package com.crewly.activity

import android.app.Activity
import android.content.Intent
import com.crewly.account.AccountActivity
import com.crewly.auth.LoginActivity
import com.crewly.roster.list.RosterListActivity
import com.crewly.roster.details.RosterDetailsActivity
import javax.inject.Inject

/**
 * Created by Derek on 17/06/2018
 * Manages navigating to various screens in the app.
 */
@ActivityScope
class AppNavigator @Inject constructor(private val activity: Activity) {

    private val intents = mutableListOf<Intent>()

    fun start(): AppNavigator {
        intents.clear()
        return this
    }

    fun navigate() {
        if (intents.isNotEmpty()) { activity.startActivity(intents[0]) }
    }

    fun navigateToRosterScreen(): AppNavigator {
        activity.startActivity(Intent(activity, RosterListActivity::class.java))
        return this
    }

    fun navigateToRosterDetailsScreen(dateMillis: Long): AppNavigator {
        val intent = RosterDetailsActivity.getInstance(activity, dateMillis)
        activity.startActivity(intent)
        return this
    }

    fun navigateToAccountScreen(): AppNavigator {
        activity.startActivity(Intent(activity, AccountActivity::class.java))
        return this
    }

    fun navigateToLoginScreen(): AppNavigator {
        activity.startActivity(Intent(activity, LoginActivity::class.java))
        return this
    }
}