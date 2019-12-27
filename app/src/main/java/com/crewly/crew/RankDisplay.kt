package com.crewly.crew

import androidx.annotation.DrawableRes
import com.crewly.R
import com.crewly.models.Rank
import javax.inject.Inject

class RankDisplay @Inject constructor() {

  @DrawableRes
  fun getIconForRank(
    rank: Rank
  ): Int =
    when (rank) {
      Rank.CAPTAIN -> R.drawable.icon_rank_captain
      Rank.FIRST_OFFICER -> R.drawable.icon_rank_first_officer
      Rank.PU_SEP -> R.drawable.icon_rank_pu_sep
      Rank.PU_LC -> R.drawable.icon_rank_pu_lc
      Rank.PU -> R.drawable.icon_rank_pu
      Rank.JU_PU -> R.drawable.icon_rank_ju_pu
      Rank.JU -> R.drawable.icon_rank_ju
      Rank.JU_NEW -> R.drawable.icon_rank_ju_new
      else -> -1
    }
}