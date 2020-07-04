package com.crewly.utils

import io.reactivex.Completable

interface FileHelper {

  fun readAndSaveImageFromUrl(
    url: String,
    fileName: String
  ): Completable
}