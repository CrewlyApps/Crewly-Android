package com.crewly.aws

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.KeyPair
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.crewly.network.AwsFlight
import com.crewly.network.AwsModelMapper
import com.crewly.network.AwsUser
import com.crewly.models.Flight
import com.crewly.models.account.Account
import com.crewly.models.crew.Crew
import com.crewly.network.AwsTableNames
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
        mappings[AwsTableNames.USER]?.toList() as? List<AwsUser> ?: listOf()
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

  fun populateCrewForFlights(
    flights: List<Flight>
  ): Single<List<Flight>> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        mapper.batchLoad(mapOf<Class<*>, List<KeyPair>>(
          AwsFlight::class.java to flights.map { flight ->
            val awsFlight = awsModelMapper.flightToAwsFlight(
              flight = flight
            )
            KeyPair().apply {
              withHashKey(awsFlight.id)
              withRangeKey(awsFlight.companyId)
            }
          }
        ))
      }
      .map { mappings ->
        mappings[AwsTableNames.FLIGHT]?.toList() as? List<AwsFlight> ?: listOf()
      }
      .map { awsFlights ->
        flights.map { flight ->
          awsFlights.find { awsFlight ->
            awsFlight.id == awsModelMapper.generateAwsFlightId(flight)
          }?.let { awsFlight ->
            flight.copy(
              departureSector = flight.departureSector.copy(
                crew = awsFlight.crewIds.toMutableList()
              ),

              arrivalSector = flight.arrivalSector.copy(
                crew = awsFlight.crewIds.toMutableList()
              )
            )
          } ?: flight
        }
      }

  fun getFlightsForCrewMember(
    crewCode: String
  ): Single<List<Flight>> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        mapper.scan(
          AwsFlight::class.java,
          DynamoDBScanExpression()
            .withFilterExpression("contains(crew, :ownerId)")
            .withExpressionAttributeValues(
              mapOf(
                ":ownerId" to AttributeValue(crewCode)
              )
            )
        )
      }
      .map { awsFlights ->
        awsFlights.map { awsFlight -> awsModelMapper.awsFlightToFlight(awsFlight) }
      }

  fun createOrUpdateFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        val awsFlight = awsModelMapper.flightToAwsFlight(
          flight = flight
        )

        mapper.save(
          awsFlight,
          DynamoDBMapperConfig.Builder()
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build())
      }
      .ignoreElement()

  fun createOrUpdateFlights(
    flights: List<Flight>
  ): Completable =
    flights
      .map { flight ->
        createOrUpdateFlight(
          flight = flight
        )
      }
      .reduce { currentCompletable, nextCompletable ->
        currentCompletable.mergeWith(nextCompletable)
      }

  fun deleteFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper ->
        val awsFlight = awsModelMapper.flightToAwsFlight(
          flight = flight
        )

        mapper.delete(awsFlight)
      }
      .ignoreElement()

  fun deleteFlights(
    flights: List<Flight>
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper ->
        mapper.batchDelete(flights.map { flight ->
          val awsFlight = awsModelMapper.flightToAwsFlight(
            flight = flight
          )

          awsFlight
        })
      }
      .ignoreElement()

  private fun getCrewIdsForFlight(
    flight: Flight
  ): Single<List<String>> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        val awsFlight = awsModelMapper.flightToAwsFlight(
          flight = flight
        )
        mapper.load(AwsFlight::class.java, awsFlight.id, awsFlight.companyId)
      }
      .map { awsFlight -> awsFlight.crewIds.toList() }
}