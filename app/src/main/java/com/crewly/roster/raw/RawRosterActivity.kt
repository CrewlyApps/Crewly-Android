package com.crewly.roster.raw

import android.os.Bundle
import com.crewly.R
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable

class RawRosterActivity : DaggerAppCompatActivity() {

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.raw_roster_activity)
  }

  override fun onDestroy() {
    disposables.dispose()
    super.onDestroy()
  }
}