package com.hushbunny.app.core

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.forEach
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ActivityMainBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.home.HomeViewModel
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.uitls.viewModelBuilder
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var homeRepository: HomeRepository

    private val homeViewModel: HomeViewModel by viewModelBuilder {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = homeRepository
        )
    }

    private val homeSharedViewModel: HomeSharedViewModel by viewModelBuilder {
        HomeSharedViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (application as AppComponentProvider).getAppComponent().inject(this)

        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        binding.bottomNav.setupWithNavController(navHostFragment.navController)

        binding.bottomNav.menu.forEach {
            binding.bottomNav.findViewById<View>(it.itemId)
                .setOnLongClickListener { true }
        }
        binding.bottomNav.setOnItemSelectedListener {
            navigateToBottomItem(it)
            when (it.toString()) {
                resourceProvider.getString(R.string.title_home) -> {
                    homeSharedViewModel.onHomeTabClicked()
                    navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                }
                resourceProvider.getString(R.string.title_notifications) -> {
                    homeSharedViewModel.onNotificationTabClicked()
                    navHostFragment.navController.popBackStack(R.id.notificationFragment, false)
                }
                resourceProvider.getString(R.string.title_profile) -> {
                    homeSharedViewModel.onProfileTabClicked()
                    navHostFragment.navController.popBackStack(R.id.profileFragment, false)
                }
            }
            true
        }
        setObserver()
    }

    private fun setObserver() {
        homeViewModel.notificationCountObserver.observe(this) {
            updateNotificationBadge(it?.data?.unreadCount ?: 0)
        }
    }

    private fun updateNotificationBadge(count: Int) {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.notificationFragment)
        badge.number = count
        badge.isVisible = count > 0
    }

    private fun navigateToBottomItem(menuItem: MenuItem) {
        try {
            NavigationUI.onNavDestinationSelected(
                menuItem,
                Navigation.findNavController(this, R.id.nav_host_fragment)
            )
        } catch (e: Exception) {
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun startBottomNavigationDownToUpAnimation() {
        val animUp = AnimationUtils.loadAnimation(
            this,
            R.anim.bottom_navigation_bar_slide_up
        )
        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                setBottomNavigationVisibility(View.VISIBLE)
            }

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.bottomNav.startAnimation(animUp)
    }

    private fun startBottomNavigationUpToDownAnimation() {
        val animDown = AnimationUtils.loadAnimation(
            this,
            R.anim.bottom_navigation_bar_slide_down
        )
        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                setBottomNavigationVisibility(View.GONE)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.bottomNav.startAnimation(animDown)
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNav.menu.forEach {
            binding.bottomNav.findViewById<View>(it.itemId)
                .setOnLongClickListener { true }
        }
        homeViewModel.getNotificationCount()
    }

    override fun setBottomNavigationVisibility(visibility: Int) {
        binding.bottomNav.visibility = visibility
        binding.addMomentImage.visibility = visibility
    }

    override fun animateBottomNavigation(hide: Boolean) {
        super.animateBottomNavigation(hide)
        if (hide) {
            startBottomNavigationUpToDownAnimation()
        } else {
            startBottomNavigationDownToUpAnimation()
        }
    }
}