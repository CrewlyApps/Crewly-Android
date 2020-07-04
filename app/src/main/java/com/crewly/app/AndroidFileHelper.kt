package com.crewly.app

import android.content.Context
import com.crewly.utils.FileHelper
import io.reactivex.Completable
import java.net.URL
import javax.inject.Inject

class AndroidFileHelper @Inject constructor(
  private val context: Context
): FileHelper {

  override fun readAndSaveImageFromUrl(
    url: String,
    fileName: String
  ): Completable =
    Completable.create {
      URL(url).openStream().use { inputStream ->
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
          inputStream.copyTo(outputStream)
        }
      }

      it.onComplete()
    }
}