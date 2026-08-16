package com.islamichub.app

import android.app.Application
import com.islamichub.app.data.AppContainer

class IslamicHubApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
    }

    companion object {
        lateinit var instance: IslamicHubApp
            private set
    }
}
