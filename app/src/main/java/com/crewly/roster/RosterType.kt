package com.crewly.roster

/**
 * Created by Derek on 30/05/2018
 */
sealed class RosterType {

    object HSBY: RosterType()
    object ASBY: RosterType()
    object Duty: RosterType()
    object Sick: RosterType()
    object Off: RosterType()
}