package com.crewly.persistence

import com.crewly.models.file.FileData
import com.crewly.models.file.FileFormat
import com.crewly.utils.FileHelper
import io.reactivex.Completable
import javax.inject.Inject

class RawRosterFileHelper @Inject constructor(
  private val fileHelper: FileHelper
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
    fileHelper.readAndSaveImageFromUrl(
      url = data.imageUrl,
      fileName = data.fileName
    )

  private fun getExtensionForFileFormat(
    fileFormat: FileFormat
  ): String =
    when (fileFormat) {
      FileFormat.JPEG -> ".jpg"
      FileFormat.PDF -> ".pdf"
    }
}