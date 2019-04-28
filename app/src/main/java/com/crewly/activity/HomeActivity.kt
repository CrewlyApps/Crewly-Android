package com.crewly.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.crewly.R
import com.crewly.account.AccountFragment
import com.crewly.logbook.LogbookFragment
import com.crewly.roster.list.RosterListFragment
import com.crewly.utils.replaceAndShow
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.home_activity.*

/**
 * Created by Derek on 27/04/2019
 */
class HomeActivity: DaggerAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
    setUpBottomNavView()
    showFragment(RosterListFragment())
  }

  override fun onBackPressed() {
    val accountFragment = supportFragmentManager
      .findFragmentByTag(AccountFragment::class.qualifiedName)

    val handled = (accountFragment as? AccountFragment)?.onBackPressed() ?: false

    if (!handled) super.onBackPressed()
  }

  private fun setUpBottomNavView() {
    bottom_nav_view.setOnNavigationItemSelectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.menu_roster -> showFragment(RosterListFragment())
        R.id.menu_logbook -> showFragment(LogbookFragment())
        R.id.menu_account -> showFragment(AccountFragment())
      }

      true
    }
  }

  private fun showFragment(fragment: Fragment) {
    replaceAndShow(
      fragment = fragment,
      container = R.id.container_fragment
    )
  }
}