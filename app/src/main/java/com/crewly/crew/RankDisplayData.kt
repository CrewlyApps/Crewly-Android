package com.crewly.crew

import androidx.annotation.DrawableRes
import com.crewly.models.Rank

data class RankDisplayData(
  val rank: Rank,
  @DrawableRes val iconRes: Int
)