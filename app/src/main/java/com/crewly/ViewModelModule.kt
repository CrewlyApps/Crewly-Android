package com.crewly

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.crewly.roster.RosterViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Derek on 27/05/2018
 */
@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.AndroidViewModelFactory

    @Binds
    @IntoMap
    @ViewModelFactory.ViewModelKey(RosterViewModel::class)
    abstract fun bindMainActivityViewModel(viewModel: RosterViewModel): ViewModel
}