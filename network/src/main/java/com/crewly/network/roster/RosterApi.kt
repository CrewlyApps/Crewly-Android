package com.crewly.network.roster

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface RosterApi {

  @POST("/roster/updater")
  fun triggerRosterFetch(): Single<String>

  @GET("/job/{jobId}")
  fun checkJobStatus(
    @Path("jobId") jobId: String
  ): Completable

  @FormUrlEncoded
  @POST("/roster")
  fun fetchRoster(
    @Field("username") username: String,
    @Field("password") password: String,
    @Field("company") companyId: Int
  ): Single<NetworkRosterResponse>
}