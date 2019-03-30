package com.crewly.duty

import android.annotation.SuppressLint
import android.content.Context
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.utils.readAssetsFile
import com.squareup.moshi.Moshi
import io.reactivex.Scheduler
import org.json.JSONArray
import javax.inject.Named

/**
 * Created by Derek on 24/07/2018
 */
class AirportHelper(
  private val context: Context,
  private val crewlyPreferences: CrewlyPreferences,
  private val crewlyDatabase: CrewlyDatabase,
  private val moshi: Moshi,
  @Named(RxModule.IO_THREAD
  ) private val ioThread: Scheduler
) {

  @SuppressLint("CheckResult")
  fun copyAirportsToDatabase() {
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
      .subscribe { airports ->
        crewlyDatabase
          .airportDao()
          .insertAirports(airports)
        crewlyPreferences.saveAirportDataCopied()
      }
  }
}