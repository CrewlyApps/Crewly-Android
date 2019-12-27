package com.crewly.activity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.crewly.R
import com.crewly.account.AccountFragment
import com.crewly.account.AccountManager
import com.crewly.logbook.LogbookFragment
import com.crewly.roster.list.RosterListFragment
import com.crewly.utils.findFragment
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.home_activity.*
import javax.inject.Inject

/**
 * Created by Derek on 27/04/2019
 */
class HomeActivity: DaggerAppCompatActivity() {

  @Inject lateinit var accountManager: AccountManager

  private val disposables = CompositeDisposable()
  private var activeFragment: Fragment = RosterListFragment()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
    observeAccountSwitchEvents()
    setUpBottomNavView()
    showBottomNav()
    showFragment(activeFragment, true)
  }

  override fun onDestroy() {
    disposables.dispose()
    super.onDestroy()
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

  private fun observeAccountSwitchEvents() {
    disposables + accountManager
      .observeAccountSwitchEvents()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { account ->
        bottom_nav_view.isVisible = account.crewCode.isNotEmpty()
      }
  }

  private fun showBottomNav() {
    disposables + accountManager
      .observeCurrentAccount()
      .observeOn(AndroidSchedulers.mainThread())
      .take(1)
      .subscribe { account ->
        bottom_nav_view.isVisible = account.crewCode.isNotEmpty()
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