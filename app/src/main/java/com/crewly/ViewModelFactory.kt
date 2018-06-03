package com.crewly

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.MapKey
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Created by Derek on 27/05/2018
 */
@Singleton
class ViewModelFactory @Inject constructor(application: Application,
                                           private val creators: Map<Class<out ViewModel>,
                                                   @JvmSuppressWildcards Provider<ViewModel>>):
        ViewModelProvider.AndroidViewModelFactory(application) {

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @MapKey
    annotation class ViewModelKey(val key: KClass<out ViewModel>)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?:
        creators.asIterable().firstOrNull() { modelClass.isAssignableFrom(it.key) }?.value ?:
        throw IllegalArgumentException("Unknown model class $modelClass")

        return creator.get() as T
    }
}