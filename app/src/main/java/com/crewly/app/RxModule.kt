package com.crewly.app

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Derek on 02/06/2018
 */
@Module
class RxModule {

    companion object {
        const val IO_THREAD = "IoThread"
        const val MAIN_THREAD = "MainThread"
    }

    @Singleton
    @Provides
    @Named(IO_THREAD)
    fun provideIoThread(): Scheduler = Schedulers.io()

    @Singleton
    @Provides
    @Named(MAIN_THREAD)
    fun provideMainThread(): Scheduler = AndroidSchedulers.mainThread()
}