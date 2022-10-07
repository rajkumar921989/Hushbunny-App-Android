package com.hushbunny.app.di

import com.hushbunny.app.application.ApplicationScopeProvider
import com.hushbunny.app.application.BaseApplication
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.providers.ResourceProviderImp
import com.hushbunny.app.ui.navigation.NavigationRouterProvider
import com.hushbunny.app.ui.navigation.NavigationRouterProviderImpl
import com.hushbunny.app.ui.network.*
import com.hushbunny.app.ui.network.NetworkMonitorImp
import com.hushbunny.app.ui.repository.*
import com.hushbunny.app.ui.service.*
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
        return RetrofitClient(application.applicationContext)
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

    @Provides
    @Singleton
    fun providerHomeService(resourceProvider: ResourceProvider, networkCallHandler: NetworkCallHandler): HomeService {
        return HomeServiceImpl(resourceProvider, networkCallHandler)
    }

    @Provides
    @Singleton
    fun providerHomeRepository(resourceProvider: ResourceProvider, homeService: HomeService): HomeRepository {
        return HomeRepositoryImpl(resourceProvider = resourceProvider, homeService = homeService)
    }

    @Provides
    @Singleton
    fun providerUserActionService(resourceProvider: ResourceProvider, networkCallHandler: NetworkCallHandler): UserActionService {
        return UserActionServiceImpl(resourceProvider, networkCallHandler)
    }

    @Provides
    @Singleton
    fun providerUserActionRepository(userActionService: UserActionService): UserActionRepository {
        return UserActionRepositoryImpl(userActionService)
    }

    @Provides
    @Singleton
    fun providerFileUploadService(resourceProvider: ResourceProvider, networkCallHandler: NetworkCallHandler): FileUploadService {
        return FileUploadServiceImpl(resourceProvider, networkCallHandler)
    }

    @Provides
    @Singleton
    fun providerFileUploadRepository(fileUploadService: FileUploadService): FileUploadRepository {
        return FileUploadRepositoryImpl(fileUploadService)
    }

    @Provides
    @Singleton
    fun providerMomentService(resourceProvider: ResourceProvider, networkCallHandler: NetworkCallHandler): MomentService {
        return MomentServiceImpl(resourceProvider, networkCallHandler)
    }

    @Provides
    @Singleton
    fun providerMomentRepository(resourceProvider: ResourceProvider, momentService: MomentService): MomentRepository {
        return MomentRepositoryImpl(resourceProvider = resourceProvider, momentService = momentService)
    }

    @Provides
    @Singleton
    fun provideNavigationRouter(resourceProvider: ResourceProvider): NavigationRouterProvider = NavigationRouterProviderImpl(resourceProvider)
}