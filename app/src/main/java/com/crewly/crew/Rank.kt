package com.crewly.crew

import android.support.annotation.DrawableRes
import com.crewly.R

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
  }

  fun getValue(): Int = rank

  fun getName(): String {
    return when (rank) {
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

  @DrawableRes
  fun getIconRes(): Int {
    return when (rank) {
      1 -> R.drawable.icon_rank_captain
      2 -> R.drawable.icon_rank_first_officer
      3 -> R.drawable.icon_rank_pu_sep
      4 -> R.drawable.icon_rank_pu_lc
      5 -> R.drawable.icon_rank_pu
      6 -> R.drawable.icon_rank_ju_pu
      7 -> R.drawable.icon_rank_ju
      8 -> R.drawable.icon_rank_ju_new
      else -> -1
    }
  }
}