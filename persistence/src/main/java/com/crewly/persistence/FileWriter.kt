package com.crewly.persistence

import android.content.Context
import com.crewly.models.file.FileData
import com.crewly.models.file.FileFormat
import io.reactivex.Completable
import javax.inject.Inject

class FileWriter @Inject constructor(
  private val context: Context
) {

  fun getExtensionForFileFormat(
    fileFormat: FileFormat
  ): String =
    when (fileFormat) {
      FileFormat.PDF -> ".pdf"
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
}