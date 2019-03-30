package com.crewly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crewly.account.AccountViewModel
import com.crewly.auth.LoginViewModel
import com.crewly.logbook.LogbookViewModel
import com.crewly.roster.details.RosterDetailsViewModel
import com.crewly.roster.list.RosterListViewModel
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
  @ViewModelFactory.ViewModelKey(AccountViewModel::class)
  abstract fun bindAccountViewModel(viewModel: AccountViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelFactory.ViewModelKey(LogbookViewModel::class)
  abstract fun bindLogbookViewModel(viewModel: LogbookViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelFactory.ViewModelKey(LoginViewModel::class)
  abstract fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelFactory.ViewModelKey(RosterDetailsViewModel::class)
  abstract fun bindRosterDetailsViewModel(viewModel: RosterDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelFactory.ViewModelKey(RosterListViewModel::class)
  abstract fun bindRosterListViewModel(viewModel: RosterListViewModel): ViewModel
}