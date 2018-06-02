package com.crewly.app

import android.app.Application
import com.crewly.ActivityModule
import com.crewly.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * Created by Derek on 27/05/2018
 */
@Singleton
@Component(modules = [AndroidInjectionModule::class, AppModule::class, ActivityModule::class,
    ViewModelModule::class, RepositoryModule::class, RxModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance fun application(app: Application): Builder
        fun build(): AppComponent
    }

    fun inject(app: CrewlyApp)
}