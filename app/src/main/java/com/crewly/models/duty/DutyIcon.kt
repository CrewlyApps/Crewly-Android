package com.crewly.models.duty

/**
 * Created by Derek on 22/04/2019
 */
interface DutyIcon {

  companion object {
    const val NO_ICON = -1
  }

  val iconResourceId: Int
}

object NoDutyIcon: DutyIcon {
  override val iconResourceId: Int = DutyIcon.NO_ICON
}