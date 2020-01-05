package com.crewly.network.roster

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RosterApi {

  @POST("roster/updater")
  fun triggerRosterFetch(
    @Body params: Map<String, String>
  ): Single<Response<ResponseBody>>

  @POST("notification/confirm")
  fun confirmPendingNotification(
    @Body params: Map<String, String>
  ): Completable

  @GET("job/{jobId}")
  fun checkJobStatus(
    @Path("jobId") jobId: String
  ): Single<RosterJobStatus>

  @POST("roster")
  fun fetchRoster(
    @Body params: Map<String, String>
  ): Single<RosterFetchResponse>
}