package com.crewly.roster

import android.annotation.SuppressLint
import com.crewly.app.RxModule
import com.crewly.aws.AwsRepository
import com.crewly.duty.Airport
import com.crewly.duty.Flight
import com.crewly.duty.Sector
import com.crewly.logging.LoggingManager
import com.crewly.repositories.CrewRepository
import io.reactivex.Completable
import io.reactivex.Scheduler
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
    awsRepository
      .getFlightsForCrewMember(
        crewCode = crewCode
      )
      .onErrorReturn { listOf() }
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
      .flatMapCompletable { (flights, crew) ->
        rosterRepository.deleteRosterFromToday()
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

  @SuppressLint("CheckResult")
  fun updateNetworkFlights(
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
          .map { sector ->
            // Populate sectors with network flight data
            networkFlights.find { networkFlight ->
              val hasNetworkFlight = sector == networkFlight.departureSector
              if (hasNetworkFlight) {
                sector.crew = networkFlight.departureSector.crew
              }
              hasNetworkFlight
            }

            sector
          }

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
      .flatMapCompletable { (flightsToDelete, flightsToSave) ->
        awsRepository
          .deleteFlights(
            flights = flightsToDelete
          )
          .mergeWith(awsRepository.createOrUpdateFlights(
            flights = flightsToSave
          ))
      }
      .subscribeOn(ioThread)
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }
}