package com.crewly.aws

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.KeyPair
import com.crewly.aws.models.AwsFlight
import com.crewly.aws.models.AwsUser
import com.crewly.duty.Flight
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 28/04/2019
 */
class AwsRepository @Inject constructor(
  private val awsManager: AwsManager
) {

  fun getUser(
    userId: String,
    companyId: Int
  ): Single<AwsUser> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper -> mapper.load(AwsUser::class.java, userId, companyId) }

  fun getUsers(
    userIds: List<Pair<String, Int>>
  ): Single<List<AwsUser>> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        mapper.batchLoad(mapOf<Class<*>, List<KeyPair>>(
          AwsUser::class.java to userIds.map { (id, companyId) ->
            KeyPair().apply {
              withHashKey(id)
              withRangeKey(companyId)
            }
          }
        ))
      }
      .map { mappings ->
        mappings[AwsUser::class.java.toString()]?.toList() as? List<AwsUser> ?: listOf()
      }

  fun createOrUpdateUser(
    user: AwsUser
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.save(user) }
      .ignoreElement()

  fun deleteUser(
    userId: String,
    companyId: Int
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper ->
        mapper.delete(
          AwsUser().apply {
            id = userId
            this.companyId = companyId
          }
        )}
      .ignoreElement()

  fun getCrewIdsForFlight(
    flight: Flight
  ): Single<List<String>> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper -> mapper.load(AwsFlight::class.java, "id", "id") }
      .map { awsFlight -> awsFlight.crewIds.toList() }

  fun getCrewForFlight(
    flight: Flight
  ): Single<List<AwsUser>> =
    getCrewIdsForFlight(flight)
      .flatMap { crewIds -> getUsers(crewIds.map { id -> id to 0 }) }

  fun createOrUpdateFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.save(AwsFlight()) }
      .ignoreElement()

  fun createOrUpdateFlight(
    flights: List<Flight>
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.batchSave(flights) }
      .ignoreElement()

  fun deleteFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.delete(flight) }
      .ignoreElement()
}