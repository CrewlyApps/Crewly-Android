package com.crewly.duty

import android.content.Context
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.squareup.moshi.Moshi
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import org.json.JSONArray
import java.nio.charset.Charset
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
        Flowable.create<Airport>({ subscriber ->
            val json = readAssetFile("airports.json")
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
                .subscribeOn(ioThread)
                .observeOn(ioThread)
                .subscribe { airport ->
                    crewlyDatabase
                            .airportDao()
                            .insertAirport(airport)
                }
    }

    private fun readAssetFile(fileName: String): String {
        val input = context.assets.open(fileName)
        val size = input.available()
        val buffer = ByteArray(size)
        input.read(buffer)
        input.close()
        return buffer.toString(Charset.forName("UTF-8"))
    }
}