package com.crewly.persistence

import androidx.room.TypeConverter

/**
 * Created by Derek on 13/06/2018
 */
class DatabaseConverters {

  @TypeConverter
  fun fromCrewString(crewString: String): MutableList<String> {
    val crew = crewString.split(Regex(",,,"))
    return crew.toMutableList()
  }

  @TypeConverter
  fun toCrewString(crew: MutableList<String>): String {
    var crewString = ""

    crew.forEachIndexed { index, code ->
      crewString = if (index == 0) {
        crewString.plus(code)
      } else {
        crewString.plus(",,,$code")
      }
    }

    return crewString
  }
}