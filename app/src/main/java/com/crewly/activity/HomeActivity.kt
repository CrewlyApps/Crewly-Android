package com.crewly.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.crewly.R
import com.crewly.account.AccountFragment
import com.crewly.logbook.LogbookFragment
import com.crewly.roster.list.RosterListFragment
import com.crewly.utils.findFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.home_activity.*

/**
 * Created by Derek on 27/04/2019
 */
class HomeActivity: DaggerAppCompatActivity() {

  private var activeFragment: Fragment = RosterListFragment()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
    setUpBottomNavView()
    showFragment(activeFragment, true)
  }

  override fun onBackPressed() {
    val handled = findFragment<AccountFragment>()?.onBackPressed() ?: false
    if (!handled) super.onBackPressed()
  }

  private fun setUpBottomNavView() {
    bottom_nav_view.setOnNavigationItemSelectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.menu_roster -> {
          val fragment = findFragment<RosterListFragment>()
          showFragment(fragment ?: RosterListFragment(), fragment == null)
        }

        R.id.menu_logbook -> {
          val fragment = findFragment<LogbookFragment>()
          showFragment(fragment ?: LogbookFragment(), fragment == null)
        }

        R.id.menu_account -> {
          val fragment = findFragment<AccountFragment>()
          showFragment(fragment ?: AccountFragment(), fragment == null)
        }
      }

      true
    }
  }

  private fun showFragment(
    fragment: Fragment,
    add: Boolean
  ) {
    supportFragmentManager.beginTransaction().apply {
      if (add) add(R.id.container_fragment, fragment, fragment::class.java.name)
      hide(activeFragment)
      show(fragment)
      commit()
    }

    activeFragment = fragment
  }
}