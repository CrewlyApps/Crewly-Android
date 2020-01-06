package com.crewly.persistence

import android.content.Context
import com.crewly.models.file.FileData
import com.crewly.models.file.FileFormat
import io.reactivex.Completable
import javax.inject.Inject

class RawRosterFileHelper @Inject constructor(
  private val context: Context
) {

  fun getRawRosterFileName(
    username: String,
    fileFormat: FileFormat
  ): String {
    val extension = getExtensionForFileFormat(fileFormat)
    return "$username-raw-roster$extension"
  }

  fun writeFile(
    data: FileData
  ): Completable =
    Completable.fromCallable {
      context.openFileOutput(data.fileName, Context.MODE_PRIVATE).run {
        write(data.rawData)
        flush()
        close()
      }
    }

  private fun getExtensionForFileFormat(
    fileFormat: FileFormat
  ): String =
    when (fileFormat) {
      FileFormat.PDF -> ".pdf"
    }
}