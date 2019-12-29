package com.crewly.models

/**
 * Created by Derek on 23/06/2018
 * The rank a crew member can have in their company.
 */
enum class Rank(
  private val rank: Int
) {

  NONE(0),
  CAPTAIN(1),
  FIRST_OFFICER(2),
  PU_SEP(3),
  PU_LC(4),
  PU(5),
  JU_PU(6),
  JU(7),
  JU_NEW(8);

  companion object {
    private const val UNKNOWN = "Unknown"
    private const val NAME_CAPTAIN = "Captain"
    private const val NAME_FIRST_OFFICER = "First Officer"
    private const val NAME_PU_SEP = "PU SEP"
    private const val NAME_PU_LC = "PU LC"
    private const val NAME_PU = "PU"
    private const val NAME_JU_PU = "JU PU"
    private const val NAME_JU = "JU"
    private const val NAME_JU_NEW = "JU NEW"

    fun fromRank(rank: Int): Rank {
      return when (rank) {
        1 -> CAPTAIN
        2 -> FIRST_OFFICER
        3 -> PU_SEP
        4 -> PU_LC
        5 -> PU
        6 -> JU_PU
        7 -> JU
        8 -> JU_NEW
        else -> NONE
      }
    }

    //TODO - populate new ranks
    fun fromName(
      name: String
    ): Rank =
      when (name) {
        NAME_CAPTAIN -> CAPTAIN
        else -> NONE
      }
  }

  fun getValue(): Int = rank

  fun getName(): String {
    return when (rank) {
      0 -> UNKNOWN
      1 -> NAME_CAPTAIN
      2 -> NAME_FIRST_OFFICER
      3 -> NAME_PU_SEP
      4 -> NAME_PU_LC
      5 -> NAME_PU
      6 -> NAME_JU_PU
      7 -> NAME_JU
      8 -> NAME_JU_NEW
      else -> ""
    }
  }
}