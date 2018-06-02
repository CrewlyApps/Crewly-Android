package com.crewly

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import javax.inject.Inject

/**
 * Created by Derek on 27/05/2018
 */
class MainActivityViewModel @Inject constructor(application: Application):
        AndroidViewModel(application) {
}