package com.crewly.duty

import android.content.Context
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.utils.readAssetsFile
import com.squareup.moshi.Moshi
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import org.json.JSONArray
import javax.inject.Named

/**
 * Created by Derek on 24/07/2018
 */
class AirportHelper(private val context: Context,
                    private val crewlyPreferences: CrewlyPreferences,
                    private val crewlyDatabase: CrewlyDatabase,
                    private val moshi: Moshi,
                    @Named(RxModule.IO_THREAD) private val ioThread: Scheduler) {

    fun copyAirportsToDatabase() {
        context.readAssetsFile("airports.json")
                .subscribeOn(ioThread)
                .observeOn(ioThread)
                .toFlowable(BackpressureStrategy.BUFFER)
                .flatMap { json ->
                    Flowable.create<Airport>({ subscriber ->
                        val jsonArray = JSONArray(json)
                        val arrayLength = jsonArray.length()

                        for (i in 0 until arrayLength) {
                            val airportJson = jsonArray[i].toString()
                            val airport = moshi.adapter(Airport::class.java).fromJson(airportJson) ?: Airport()
                            subscriber.onNext(airport)
                        }

                        crewlyPreferences.saveAirportDataCopied()
                        subscriber.onComplete()

                    }, BackpressureStrategy.BUFFER)
                }
                .subscribe { airport ->
                    crewlyDatabase
                            .airportDao()
                            .insertAirport(airport)
                }
    }
}