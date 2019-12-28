package com.crewly.repositories

import com.crewly.network.RosterApi
import io.reactivex.Single
import javax.inject.Inject

class RosterNetworkRepository @Inject constructor(
  private val rosterApi: RosterApi
) {

  fun fetchRoster(
    username: String,
    password: String,
    companyId: Int
  ): Single<Unit> =
    rosterApi.fetchRoster(
      username = username,
      password = password,
      companyId = companyId
    )
}