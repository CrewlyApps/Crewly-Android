package com.crewly.roster

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.app.RxModule
import com.crewly.utils.plus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_activity.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 27/05/2018
 */
class RosterActivity: AppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

    private lateinit var viewModel: RosterViewModel
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roster_activity)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterViewModel::class.java]
        observeScreenState()
        observeRoster()
    }

    private fun observeRoster() {
        disposables + viewModel.observeRoster()
                .observeOn(mainThread)
                .subscribe { roster ->
                    val rosterMonthView = RosterMonthView(this)
                    rosterMonthView.rosterMonth = roster[0]
                    container_screen.addView(rosterMonthView)
                }
    }

    private fun observeScreenState() {
        disposables + viewModel.observeScreenState()
                .subscribe { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {}
                        is ScreenState.Success -> {}
                        is ScreenState.NetworkError -> {}
                        is ScreenState.Error -> {}
                    }
                }
    }
}
