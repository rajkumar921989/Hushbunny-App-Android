package com.hushbunny.app.di

import com.hushbunny.app.ui.landing.SplashActivity
import com.hushbunny.app.ui.onboarding.OnBoardingActivity
import com.hushbunny.app.ui.onboarding.login.LoginFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppComponentModule::class]
)

interface AppComponent {
    fun inject(activity: SplashActivity)
    fun inject(activity: OnBoardingActivity)
    fun inject(activity: LoginFragment)
}