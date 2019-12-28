package com.crewly.repositories

import com.crewly.network.roster.NetworkRoster
import com.crewly.network.roster.RosterApi
import io.reactivex.Single
import javax.inject.Inject

class RosterNetworkRepository @Inject constructor(
  private val rosterApi: RosterApi
) {

  fun fetchRoster(
    username: String,
    password: String,
    companyId: Int
  ): Single<NetworkRoster> =
    rosterApi.fetchRoster(
      username = username,
      password = password,
      companyId = companyId
    )
      .map { it.roster }
}