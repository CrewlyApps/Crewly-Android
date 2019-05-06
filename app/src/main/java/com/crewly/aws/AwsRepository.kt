package com.crewly.aws

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.KeyPair
import com.crewly.account.Account
import com.crewly.account.AccountManager
import com.crewly.aws.models.AwsFlight
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
  private val accountManager: AccountManager,
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

  fun populateCrewForFlights(
    flights: List<Flight>
  ): Single<List<Flight>> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        mapper.batchLoad(mapOf<Class<*>, List<KeyPair>>(
          AwsFlight::class.java to flights.map { flight ->
            val awsFlight = awsModelMapper.flightToAwsFlight(
              crewId = accountManager.getCurrentAccount().crewCode,
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
        mappings[AwsFlight::class.java.toString()]?.toList() as? List<AwsFlight> ?: listOf()
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

  fun getCrewForFlight(
    flight: Flight
  ): Single<List<Crew>> =
    getCrewIdsForFlight(flight)
      .flatMap { crewIds -> getCrewMembers(crewIds.map { id -> id to flight.departureSector.company.id }) }

  fun createOrUpdateFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        mapper.save(awsModelMapper.flightToAwsFlight(
          crewId = accountManager.getCurrentAccount().crewCode,
          flight = flight
        ))
      }
      .ignoreElement()

  fun createOrUpdateFlights(
    flights: List<Flight>
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .map { mapper ->
        mapper.batchSave(flights.map { flight ->
          awsModelMapper.flightToAwsFlight(
            crewId = accountManager.getCurrentAccount().crewCode,
            flight = flight
          )
        })
      }
      .ignoreElement()

  fun deleteFlight(
    flight: Flight
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper ->
        mapper.delete(awsModelMapper.flightToAwsFlight(
          crewId = accountManager.getCurrentAccount().crewCode,
          flight = flight
        ))
      }
      .ignoreElement()

  fun deleteFlights(
    flights: List<Flight>
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper ->
        mapper.batchDelete(flights.map { flight ->
          awsModelMapper.flightToAwsFlight(
            crewId = accountManager.getCurrentAccount().crewCode,
            flight = flight
          )
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
          crewId = accountManager.getCurrentAccount().crewCode,
          flight = flight
        )
        mapper.load(AwsFlight::class.java, awsFlight.id, awsFlight.companyId)
      }
      .map { awsFlight -> awsFlight.crewIds.toList() }
}