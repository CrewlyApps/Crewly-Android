package com.crewly.models

/**
 * Created by Derek on 04/05/2019
 */
sealed class Company(
  val id: Int,
  val name: String
) {

  companion object {

    fun fromId(id: Int): Company =
      when (id) {
        0 -> Ryanair
        1 -> Norwegian
        else -> None
      }
  }

  object None: Company(
    id = -1,
    name = ""
  )

  object Ryanair: Company(
    id = 0,
    name = "Ryanair"
  )

  object Norwegian: Company(
    id = 1,
    name = "Norwegian"
  )
}