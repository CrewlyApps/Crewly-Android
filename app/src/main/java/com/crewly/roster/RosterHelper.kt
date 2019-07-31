package com.crewly.roster

import android.annotation.SuppressLint
import com.crewly.BuildConfig
import com.crewly.app.RxModule
import com.crewly.aws.AwsRepository
import com.crewly.db.airport.Airport
import com.crewly.db.sector.Sector
import com.crewly.logging.LoggingManager
import com.crewly.models.Flight
import com.crewly.models.roster.Roster
import com.crewly.repositories.CrewRepository
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Derek on 09/05/2019
 */
@Singleton
class RosterHelper @Inject constructor(
  private val awsRepository: AwsRepository,
  private val crewRepository: CrewRepository,
  private val rosterRepository: RosterRepository,
  private val loggingManager: LoggingManager,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
) {

  private data class SectorFetchData(
    val networkFlights: List<Flight>,
    val localSectors: List<Sector>,
    val rosterSectors: List<Sector>
  )

  private data class SectorRequestData(
    val sectorsToDelete: List<Sector>,
    val sectorsToSave: List<Sector>
  )

  private data class FlightRequestData(
    val flightsToDelete: List<Flight>,
    val flightsToSave: List<Flight>
  )

  fun saveRoster(
    crewCode: String,
    roster: Roster
  ): Completable =
    fetchUserFlights(
      crewCode = crewCode
    )
      .flatMap { flights ->
        awsRepository
          .getCrewMembers(
            userIds = flights.fold(mutableSetOf<Pair<String, Int>>()) { ids, flight ->
              flight.departureSector.crew.forEach { id ->
                ids.add(id to flight.departureSector.company.id)
              }
              ids
            }.toList()
          )
          .map { crew -> flights to crew }
      }
      // Populate roster with crew from network flights
      .doOnSuccess { (flights, _) ->
        roster.sectors.forEach { sector ->
          val networkFlight = flights.find { flight -> flight.departureSector.flightId == sector.flightId }
          if (networkFlight != null) sector.crew = networkFlight.departureSector.crew
        }
      }
      .flatMapCompletable { (flights, crew) ->
        val firstRosterDay = roster.duties.first()
        rosterRepository.deleteRosterFromDay(
          crewCode = crewCode,
          day = firstRosterDay.date
        )
          .mergeWith(rosterRepository.insertOrReplaceRoster(
            roster = roster
          ))
          .mergeWith(crewRepository.insertOrUpdateCrew(
            crew = crew
          ))
          .doOnComplete { updateNetworkFlights(
            crewCode = crewCode,
            newSectors = roster.sectors,
            flights = flights
          )}
      }

  fun clearUserRosterDataFromNetwork(
    crewCode: String
  ): Completable =
    fetchUserFlights(
      crewCode = crewCode
    )
      .map { flights ->
        val userRemovedFlights = flights.map { flight ->
          flight.departureSector.crew.remove(crewCode)
          flight
        }

        val flightsToDelete = userRemovedFlights.filter { flight ->
          flight.departureSector.crew.isEmpty()
        }

        val flightsToSave = flights.minus(flightsToDelete)

        FlightRequestData(
          flightsToDelete = flightsToDelete,
          flightsToSave = flightsToSave
        )
      }
      .flatMapCompletable { updateNetworkWithUserData(it) }

  @SuppressLint("CheckResult")
  private fun updateNetworkFlights(
    crewCode: String,
    newSectors: List<Sector>,
    flights: List<Flight>
  ) {
    rosterRepository.fetchSectorsBetween(
        crewCode = crewCode,
        startTime = DateTime.now().withTimeAtStartOfDay(),
        endTime = DateTime.now().plusDays(14).withTimeAtStartOfDay()
      ).map { localSectors ->
        // Only save the first sector of each day
        val groupedSectors = newSectors
          .sortedBy { it.departureTime.millis }
          .groupBy { it.departureTime.dayOfYear() }
          .flatMap { it.value.take(1) }

        SectorFetchData(
          networkFlights = flights,
          localSectors = localSectors,
          rosterSectors = groupedSectors
        )
      }
      .map { (networkFlights, localSectors, rosterSectors) ->
        val sectorsCrewRemovedFrom = localSectors.minus(rosterSectors)
        val sectorsToDelete = sectorsCrewRemovedFrom.filter { sector ->
          networkFlights.find { networkFlight ->
            networkFlight.departureSector == sector && networkFlight.departureSector.crew.size == 1
          } != null
        }

        val sectorsToEdit = sectorsCrewRemovedFrom.filter { sector ->
          networkFlights.find { networkFlight ->
            networkFlight.departureSector == sector && networkFlight.departureSector.crew.size > 1
          } != null
        }

        val sectorsToSave = rosterSectors
          .minus(sectorsToDelete)
          .plus(sectorsToEdit)

        SectorRequestData(
          sectorsToDelete = sectorsToDelete,
          sectorsToSave = sectorsToSave
        )
      }
      .flatMap { (sectorsToDelete, sectorsToSave) ->
        // Populate sectors with airport data
        rosterRepository
          .fetchAirportsForSectors(sectorsToDelete.union(sectorsToSave).toList())
          .map { airports ->
            val flightsToDelete = sectorsToDelete.map { sector ->
              Flight(
                departureSector = sector,
                departureAirport = airports.find { airport ->
                  airport.codeIata == sector.departureAirport
                } ?: Airport(),
                arrivalAirport = airports.find { airport ->
                  airport.codeIata == sector.arrivalAirport
                } ?: Airport()
              )
            }

            val flightsToSave = sectorsToSave.map { sector ->
              Flight(
                departureSector = sector,
                departureAirport = airports.find { airport ->
                  airport.codeIata == sector.departureAirport
                } ?: Airport(),
                arrivalAirport = airports.find { airport ->
                  airport.codeIata == sector.arrivalAirport
                } ?: Airport()
              )
            }

            FlightRequestData(
              flightsToDelete = flightsToDelete,
              flightsToSave = flightsToSave
            )
          }
      }
      .flatMapCompletable { updateNetworkWithUserData(it) }
      .subscribeOn(ioThread)
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }

  private fun fetchUserFlights(
    crewCode: String
  ): Single<List<Flight>> =
    awsRepository
      .getFlightsForCrewMember(
        crewCode = crewCode
      )
      .onErrorReturn { listOf() }

  private fun updateNetworkWithUserData(
    flightRequestData: FlightRequestData
  ): Completable =
    if (BuildConfig.DEBUG) {
      Completable.complete()
    } else {
      awsRepository
        .deleteFlights(
          flights = flightRequestData.flightsToDelete
        )
        .mergeWith(awsRepository.createOrUpdateFlights(
          flights = flightRequestData.flightsToSave
        ))
    }
}