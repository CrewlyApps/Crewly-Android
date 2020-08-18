package com.crewly.roster.raw

import android.app.ProgressDialog
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crewly.R
import com.crewly.logging.AnalyticsManger
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.raw_roster_activity.*
import javax.inject.Inject

class RawRosterActivity : DaggerAppCompatActivity() {

  @Inject lateinit var analyticsManger: AnalyticsManger
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory

  private lateinit var viewModel: RawRosterViewModel

  private var progressDialog: ProgressDialog? = null

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.raw_roster_activity)
    viewModel = ViewModelProviders.of(this, viewModelFactory)[RawRosterViewModel::class.java]

    observeRawRoster()
    observeShowLoading()
    observeCloseButtonClicks()
  }

  override fun onResume() {
    super.onResume()
    analyticsManger.recordScreenView("Raw Roster")
  }

  override fun onDestroy() {
    disposables.dispose()
    progressDialog?.dismiss()
    super.onDestroy()
  }

  private fun observeRawRoster() {
    disposables + viewModel
      .observeRawRoster()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { rawRoster ->
        Glide.with(this)
          .load("${filesDir}/${rawRoster.filePath}")
          .skipMemoryCache(true)
          .diskCacheStrategy(DiskCacheStrategy.NONE)
          .into(image_raw_roster)
      }
  }

  private fun observeShowLoading() {
    disposables + viewModel
      .observeShowLoading()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { show ->
        progressDialog?.dismiss()

        if (show) {
          progressDialog = ProgressDialog.show(this, null,
            "Loading...", true, false)
        }
      }
  }

  private fun observeCloseButtonClicks() {
    disposables + image_close
      .throttleClicks()
      .subscribe {
        finish()
      }
  }
}