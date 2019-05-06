package com.crewly.aws

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.KeyPair
import com.amazonaws.services.dynamodbv2.model.AttributeAction
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.crewly.account.Account
import com.crewly.aws.models.AwsFlight
import com.crewly.aws.models.AwsModelKeys
import com.crewly.aws.models.AwsUser
import com.crewly.duty.Flight
import com.crewly.models.Crew
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 28/04/2019
 */
class AwsRepository @Inject constructor(
  private val awsManager: AwsManager,
  private val awsModelMapper: AwsModelMapper
) {

  fun getCrewMember(
    userId: String,
    companyId: Int
  ): Single<Crew> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        awsModelMapper.awsUserToCrew(mapper.load(AwsUser::class.java, userId, companyId) )
      }

  fun getCrewMembers(
    userIds: List<Pair<String, Int>>
  ): Single<List<Crew>> =
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
      .map { awsUsers -> awsUsers.map { awsUser ->
        awsModelMapper.awsUserToCrew(awsUser)
      }}

  fun createOrUpdateUser(
    account: Account
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.save(awsModelMapper.accountToAwsUser(account)) }
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
      .map { mapper ->
        val awsFlight = awsModelMapper.flightToAwsFlight(flight)
        mapper.load(AwsFlight::class.java, awsFlight.id, awsFlight.companyId)
      }
      .map { awsFlight -> awsFlight.crewIds.toList() }

  fun getCrewForFlight(
    flight: Flight
  ): Single<List<Crew>> =
    getCrewIdsForFlight(flight)
      .flatMap { crewIds -> getCrewMembers(crewIds.map { id -> id to flight.departureSector.company.id }) }

  fun createOrUpdateFlight(
    crewId: String,
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        val awsFlight = awsModelMapper.flightToAwsFlight(flight)
        mapper.save(awsFlight)
        awsFlight
      }
      .flatMapCompletable { awsFlight -> addCrewToFlight(
        crewId = crewId,
        awsFlight = awsFlight
      )}

  fun createOrUpdateFlights(
    crewId: String,
    flights: List<Flight>
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        val awsFlights = flights.map { flight -> awsModelMapper.flightToAwsFlight(flight) }
        mapper.batchSave(awsFlights)
        awsFlights
      }
      .flatMapCompletable { awsFlights ->
        awsFlights
          .map { awsFlight ->
            addCrewToFlight(
              crewId = crewId,
              awsFlight = awsFlight
            )
          }
          .reduce { currentCompletable, nextCompletable ->
            currentCompletable.mergeWith(nextCompletable)
          }
      }

  fun deleteFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.delete(flight) }
      .ignoreElement()

  private fun addCrewToFlight(
    crewId: String,
    awsFlight: AwsFlight
  ): Completable =
    awsManager
      .getDynamoDbClient()
      .doOnSuccess { client ->
        val request = UpdateItemRequest()
          .withTableName(AwsTableNames.FLIGHT)
          .withKey(mapOf(
            AwsModelKeys.Flight.ID to AttributeValue(awsFlight.id),
            AwsModelKeys.Flight.COMPANY_ID to AttributeValue().withN(awsFlight.companyId.toString())
          ))
          .addAttributeUpdatesEntry(
            AwsModelKeys.Flight.CREW,
            AttributeValueUpdate()
              .withValue(AttributeValue(crewId))
              .withAction(AttributeAction.ADD)
          )

        client.updateItem(request)
      }
      .ignoreElement()
}