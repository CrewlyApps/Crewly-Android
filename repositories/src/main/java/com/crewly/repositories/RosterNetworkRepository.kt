package com.crewly.repositories

import com.crewly.models.file.FileData
import com.crewly.models.file.FileFormat
import com.crewly.network.roster.NetworkRoster
import com.crewly.network.roster.RosterApi
import com.crewly.network.roster.RosterJobStatus
import com.crewly.persistence.FileWriter
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RosterNetworkRepository @Inject constructor(
  private val rosterApi: RosterApi,
  private val fileWriter: FileWriter
) {

  fun triggerRosterFetch(
    username: String,
    password: String,
    companyId: Int
  ): Single<String> =
    rosterApi.triggerRosterFetch(
      params = buildAuthParams(
        username = username,
        password = password,
        companyId = companyId
      )
    )
      .map {
        when (it.code()) {
          202 -> it.headers()["location"]
          200 -> ""
          else -> throw HttpException(Response.error<String>(it.body()!!, it.raw()))
        }
      }

  fun confirmPendingNotification(
    username: String,
    password: String,
    companyId: Int
  ): Completable =
    rosterApi.confirmPendingNotification(
      params = buildAuthParams(
        username = username,
        password = password,
        companyId = companyId
      )
    )

  fun checkJobStatus(
    jobId: String
  ): Single<RosterJobStatus> =
    rosterApi.checkJobStatus(
      jobId = jobId
    )

  fun fetchRoster(
    username: String,
    password: String,
    companyId: Int
  ): Single<NetworkRoster> =
    rosterApi.fetchRoster(
      params = buildAuthParams(
        username = username,
        password = password,
        companyId = companyId
      )
    )
      .map { it.roster }

  fun fetchRawRoster(
    username: String,
    fileFormat: FileFormat,
    url: String
  ): Single<FileData> =
    rosterApi.fetchRawRoster(
      url = url
    )
      .map {
        val extension = fileWriter.getExtensionForFileFormat(fileFormat)
        val name = "$username-raw-roster$extension"

        FileData(
          name = name,
          rawData = it.body()?.bytes() ?: ByteArray(0)
        )
      }

  private fun buildAuthParams(
    username: String,
    password: String,
    companyId: Int
  ): Map<String, String> =
    mapOf(
      "username" to username,
      "password" to password,
      "company" to companyId.toString()
    )
}