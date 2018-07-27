package com.crewly.app

import android.app.Application
import com.crewly.activity.ActivityModule
import com.crewly.network.NetworkModule
import com.crewly.viewmodel.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Created by Derek on 27/05/2018
 */
@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityModule::class,
    ViewModelModule::class, RxModule::class, NetworkModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance fun application(app: Application): Builder
        fun build(): AppComponent
    }

    fun inject(app: CrewlyApp)
}