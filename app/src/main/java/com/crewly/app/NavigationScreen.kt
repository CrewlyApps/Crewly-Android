package com.crewly.app

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import com.crewly.R
import com.crewly.account.AccountActivity
import com.crewly.roster.RosterActivity

/**
 * Created by Derek on 04/06/2018
 * Defines a screen that contains a navigation drawer.
 */
interface NavigationScreen {

    var drawerLayout: DrawerLayout
    var navigationView: NavigationView
    var actionBar: ActionBar

    fun setUpNavigationDrawer(selectedMenuItem: Int = -1) {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.icon_menu)

        if (selectedMenuItem != -1) {
            navigationView.menu.findItem(selectedMenuItem).isChecked = true
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()

            when (menuItem.itemId) {
                R.id.menu_roster -> { drawerLayout.context.startActivity(Intent(drawerLayout.context, RosterActivity::class.java)) }
                R.id.menu_logbook -> {}
                R.id.menu_settings -> { drawerLayout.context.startActivity(Intent(drawerLayout.context, AccountActivity::class.java)) }
            }

            true
        }
    }
}