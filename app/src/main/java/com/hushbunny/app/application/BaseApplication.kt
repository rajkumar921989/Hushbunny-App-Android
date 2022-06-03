package com.hushbunny.app.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Base for all Applications
 * Common Application related implementations will be available here
 */
abstract class BaseApplication : Application() {
    companion object {
        @Volatile
        var applicationInstance: BaseApplication? = null
        @Volatile
        var launchActivityClass: Class<out AppCompatActivity>? = null
    }

    val applicationIoCoroutineScope = ApplicationScopeProviderImp(CoroutineScope(SupervisorJob()))
}
