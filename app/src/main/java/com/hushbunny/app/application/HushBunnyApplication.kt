package com.hushbunny.app.application

import com.hushbunny.app.BuildConfig
import com.hushbunny.app.di.AppComponent
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.di.AppComponentModule
import com.hushbunny.app.di.DaggerAppComponent
import com.hushbunny.app.ui.landing.SplashActivity
import com.hushbunny.app.uitls.PrefsManager
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class HushBunnyApplication: BaseApplication(),AppComponentProvider {

    override fun onCreate() {
        super.onCreate()
        applicationInstance = this
        launchActivityClass = SplashActivity::class.java
        PrefsManager.initialize(this)
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun getAppComponent(): AppComponent {
        if (activeComponent.contains(ActiveComponent.APP)) {
            return activeComponent[ActiveComponent.APP] as AppComponent
        }

        // remove any existing component so we can free up memory
        activeComponent.clear()

        val cartComponent = DaggerAppComponent.builder()
            .appComponentModule(AppComponentModule(this))
            .build()
        activeComponent[ActiveComponent.APP] = cartComponent

        return cartComponent
    }
    private enum class ActiveComponent {
        APP
    }

    private var activeComponent: ConcurrentHashMap<ActiveComponent, Any> = ConcurrentHashMap()

}