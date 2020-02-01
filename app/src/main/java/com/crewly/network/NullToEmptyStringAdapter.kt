package com.crewly.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

object NullToEmptyStringAdapter {

  @FromJson
  fun fromJson(
    reader: JsonReader
  ): String =
    if (reader.peek() != JsonReader.Token.NULL) {
      reader.nextString()
    } else {
      reader.nextNull<Unit>()
      ""
    }
}