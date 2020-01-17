package com.crewly.models

sealed class Rank(
  val code: String,
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
    iconRes = R.drawable.icon_rank_1
  )

  class RankTwo(
    code: String
  ): Rank(
    code = code,
    iconRes = R.drawable.icon_rank_2
  )

  class RankThree(
    code: String
  ): Rank(
    code = code,
    iconRes = R.drawable.icon_rank_3
  )

  class RankFour(
    code: String
  ): Rank(
    code = code,
    iconRes = R.drawable.icon_rank_4
  )
}