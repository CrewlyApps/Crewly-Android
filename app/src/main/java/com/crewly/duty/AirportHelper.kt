package com.crewly.duty

import android.content.Context
import com.crewly.app.RxModule
import com.crewly.db.CrewlyDatabase
import com.crewly.db.airport.Airport
import com.crewly.utils.readAssetsFile
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Scheduler
import org.json.JSONArray
import javax.inject.Named

/**
 * Created by Derek on 24/07/2018
 */
class AirportHelper(
  private val context: Context,
  private val crewlyDatabase: CrewlyDatabase,
  private val moshi: Moshi,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
) {

  fun copyAirportsToDatabase(): Completable =
    context.readAssetsFile("airports.json")
      .subscribeOn(ioThread)
      .observeOn(ioThread)
      .map { json ->
        val jsonArray = JSONArray(json)
        val arrayLength = jsonArray.length()
        val airports = mutableListOf<Airport>()

        for (i in 0 until arrayLength) {
          val airportJson = jsonArray[i].toString()
          val airport = moshi.adapter(Airport::class.java).fromJson(airportJson) ?: Airport()
          airports.add(airport)
        }

        airports
      }
      .flatMapCompletable { airports ->
        crewlyDatabase
          .airportDao()
          .insertAirports(airports)
      }
}