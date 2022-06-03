package com.hushbunny.app.di

import com.hushbunny.app.application.ApplicationScopeProvider
import com.hushbunny.app.application.BaseApplication
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.providers.ResourceProviderImp
import com.hushbunny.app.ui.network.*
import com.hushbunny.app.ui.onboarding.serviceandrepository.OnBoardingRepository
import com.hushbunny.app.ui.onboarding.serviceandrepository.OnBoardingRepositoryImpl
import com.hushbunny.app.ui.onboarding.serviceandrepository.OnBoardingService
import com.hushbunny.app.ui.onboarding.serviceandrepository.OnBoardingServiceImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppComponentModule(private val application: BaseApplication) {
    @Provides
    fun provideApplicationCoroutineScope(): ApplicationScopeProvider =
        application.applicationIoCoroutineScope

    @Singleton
    @Provides
    fun getResourceProvider(): ResourceProvider = ResourceProviderImp(application)

    @Provides
    @Singleton
    fun getNetworkCallHandler(networkClient: NetworkClient, networkMonitor: NetworkMonitor): NetworkCallHandler {
        return NetworkCallHandler(networkMonitor, networkClient)
    }

    @Provides
    @Singleton
    fun getNetworkMonitor(): NetworkMonitor {
        return NetworkMonitorImp(application)
    }

    @Provides
    @Singleton
    fun getNetworkClient(): NetworkClient {
        return RetrofitClient()
    }

    @Provides
    @Singleton
    fun providerOnBoardingService(resourceProvider: ResourceProvider, networkCallHandler: NetworkCallHandler): OnBoardingService {
        return OnBoardingServiceImpl(resourceProvider, networkCallHandler)
    }

    @Provides
    @Singleton
    fun providerOnBoardingRepository(onBoardingService: OnBoardingService): OnBoardingRepository {
        return OnBoardingRepositoryImpl(onBoardingService)
    }

}