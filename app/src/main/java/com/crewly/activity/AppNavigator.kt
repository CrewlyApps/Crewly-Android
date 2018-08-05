package com.crewly.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.crewly.account.AccountActivity
import com.crewly.auth.LoginActivity
import com.crewly.roster.details.RosterDetailsActivity
import com.crewly.roster.list.RosterListActivity
import javax.inject.Inject

/**
 * Created by Derek on 17/06/2018
 * Manages navigating to various screens in the app.
 */
@ActivityScope
class AppNavigator @Inject constructor(private val activity: AppCompatActivity) {

    private val intents = mutableListOf<Intent>()

    fun start(): AppNavigator {
        intents.clear()
        return this
    }

    fun navigate() {
        if (intents.isNotEmpty()) { activity.startActivity(intents[0]) }
    }

    fun navigateToRosterScreen(): AppNavigator {
        val intent = Intent(activity, RosterListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intents.add(intent)
        return this
    }

    fun navigateToRosterDetailsScreen(dateMillis: Long): AppNavigator {
        val intent = RosterDetailsActivity.getInstance(activity, dateMillis)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intents.add(intent)
        return this
    }

    fun navigateToAccountScreen(): AppNavigator {
        val intent = Intent(activity, AccountActivity::class.java)
        intents.add(intent)
        return this
    }

    fun navigateToLoginScreen(): AppNavigator {
        val intent = Intent(activity, LoginActivity::class.java)
        intents.add(intent)
        return this
    }
}