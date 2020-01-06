package com.crewly.roster.raw

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.raw_roster_activity.*
import java.io.File
import javax.inject.Inject

class RawRosterActivity : DaggerAppCompatActivity() {

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
        val file = File(filesDir, rawRoster.filePath)
        val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
        val page = pdfRenderer.openPage(0)
        displayPdfPage(page)
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

  private fun displayPdfPage(
    page: PdfRenderer.Page
  ) {
    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    image_raw_roster.setImageBitmap(bitmap)
  }
}