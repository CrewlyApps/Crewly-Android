package com.crewly.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.crewly.auth.LoginActivity
import com.crewly.roster.details.RosterDetailsActivity
import javax.inject.Inject

/**
 * Created by Derek on 17/06/2018
 * Manages navigating between screens in the app. Maintains a list of all screens added prior to
 * navigation in order to allow construction of a backstack. The screens will be started in the
 * order they are added to the navigator.
 */
@ActivityScope
class AppNavigator @Inject constructor(
  private val activity: AppCompatActivity
) {

  private val intents = mutableListOf<Intent>()

  fun start(): AppNavigator {
    intents.clear()
    return this
  }

  fun navigate() {
    if (intents.isNotEmpty()) {
      if (intents.size > 1) {
        activity.startActivities(intents.toTypedArray())
      } else {
        val intent = intents[0]
        if (intent.resolveActivity(activity.packageManager) != null) {
          activity.startActivity(intent)
        }
      }
    }
  }

  fun toRosterDetailsScreen(dateMillis: Long): AppNavigator {
    val intent = RosterDetailsActivity.getInstance(activity, dateMillis)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    intents.add(intent)
    return this
  }

  fun toLoginScreen(): AppNavigator {
    val intent = Intent(activity, LoginActivity::class.java)
    intents.add(intent)
    return this
  }

  fun toSendEmail(emailAddress: String): AppNavigator {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
    intents.add(intent)
    return this
  }

  fun toWebsite(url: String): AppNavigator {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intents.add(intent)
    return this
  }

  fun toPlayStorePage(): AppNavigator {
    val uri = Uri.parse("market://details?id=${activity.packageName}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    } else {
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    }

    intents.add(intent)
    return this
  }
}