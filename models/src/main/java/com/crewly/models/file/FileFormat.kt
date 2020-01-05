package com.crewly.models.file

enum class FileFormat(
  val type: String
) {
  PDF("pdf");

  companion object {

    fun fromType(
      type: String
    ): FileFormat =
      when {
        type.equals(PDF.type, true) -> PDF
        else -> PDF
      }
  }
}