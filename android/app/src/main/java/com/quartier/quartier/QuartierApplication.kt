package com.quartier.quartier

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
//Entry point of the app, created to be used with Hilt injection
@HiltAndroidApp
class QuartierApplication : Application()