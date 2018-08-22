package com.crewly.app

import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import com.crewly.R
import com.crewly.account.Account
import com.crewly.activity.AppNavigator
import kotlinx.android.synthetic.main.nav_header.view.*

/**
 * Created by Derek on 04/06/2018
 * Defines a screen that contains a navigation drawer.
 */
interface NavigationScreen {

    var appNavigator: AppNavigator

    var drawerLayout: DrawerLayout
    var navigationView: NavigationView
    var actionBar: ActionBar

    fun setUpNavigationDrawer(selectedMenuItem: Int = -1) {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.icon_menu)
        setSelectedNavigationDrawerItem(selectedMenuItem)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()

            when (menuItem.itemId) {
                R.id.menu_roster -> { appNavigator.start().toRosterScreen().navigate() }
                R.id.menu_logbook -> {}
                R.id.menu_account -> { appNavigator.start().toAccountScreen().navigate() }
            }

            true
        }
    }

    fun setUpNavigationHeader(account: Account) {
        navigationView.getHeaderView(0).text_airline.text = account.company
        navigationView.getHeaderView(0).text_username.text = account.crewCode
    }

    fun setSelectedNavigationDrawerItem(selectedMenuItem: Int = -1) {
        if (selectedMenuItem != -1) {
            navigationView.menu.findItem(selectedMenuItem).isChecked = true
        }
    }
}