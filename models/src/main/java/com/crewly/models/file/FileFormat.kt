package com.crewly.models.file

enum class FileFormat(
  val type: String
) {
  JPEG("jpg"),
  PDF("pdf");

  companion object {

    fun fromType(
      type: String
    ): FileFormat =
      when {
        type.equals(JPEG.type, true) -> JPEG
        type.equals(PDF.type, true) -> PDF
        else -> JPEG
      }
  }
}