package com.crewly.models

sealed class Rank(
  val code: String,
  val priority: Int,
  val iconRes: Int
) {

  companion object {

    fun toRank(
      code: String
    ): Rank =
      when (code) {
        "CP" -> RankOne(code)
        "FO" -> RankTwo(code)
        "CC" -> RankThree(code)
        "CM" -> RankFour(code)
        else -> RankFour(code)
      }
  }

  class RankOne(
    code: String
  ): Rank(
    code = code,
    priority = 1,
    iconRes = R.drawable.icon_rank_1
  )

  class RankTwo(
    code: String
  ): Rank(
    code = code,
    priority = 2,
    iconRes = R.drawable.icon_rank_2
  )

  class RankThree(
    code: String
  ): Rank(
    code = code,
    priority = 3,
    iconRes = R.drawable.icon_rank_3
  )

  class RankFour(
    code: String
  ): Rank(
    code = code,
    priority = 4,
    iconRes = R.drawable.icon_rank_4
  )
}