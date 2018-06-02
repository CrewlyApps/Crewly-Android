package com.crewly

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crewly.roster.RosterMonthView
import com.crewly.utils.createTestRosterMonth
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Derek on 27/05/2018
 */
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rosterMonthView = RosterMonthView(this)
        rosterMonthView.rosterDates = createTestRosterMonth()
        container_screen.addView(rosterMonthView)
    }
}
