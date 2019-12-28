package com.crewly.network

import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RosterApi {

  @FormUrlEncoded
  @POST("/roster")
  fun fetchRoster(
    @Field("username") username: String,
    @Field("password") password: String,
    @Field("company") companyId: Int
  ): Single<Unit>
}