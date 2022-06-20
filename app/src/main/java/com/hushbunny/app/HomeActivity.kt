package com.hushbunny.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.hushbunny.app.databinding.ActivityMainBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.uitls.AppConstants
import java.util.logging.Logger

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

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
        binding.bottomNav.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                }
                R.id.navigation_notifications -> {
                    navHostFragment.navController.popBackStack(R.id.notificationFragment, false)
                }
                R.id.navigation_add_moment -> {
                    navHostFragment.navController.popBackStack(R.id.addMomentFragment, false)
                }
                R.id.navigation_setting -> {
                    navHostFragment.navController.popBackStack(R.id.settingFragment, false)
                }
                R.id.navigation_profile -> {
                    navHostFragment.navController.popBackStack(R.id.profileFragment, false)
                }
            }
        }

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    if (it.itemId != binding.bottomNav.selectedItemId) {
                        navigateToBottomItem(it)
                        navHostFragment.navController.popBackStack(R.id.homeFragment, false)
                    }
                }
                R.id.navigation_notifications -> {
                    if (it.itemId != binding.bottomNav.selectedItemId) {
                        navigateToBottomItem(it)
                        navHostFragment.navController.popBackStack(R.id.notificationFragment, false)
                    }
                }
                R.id.navigation_add_moment -> {
                    if (it.itemId != binding.bottomNav.selectedItemId) {
                        navigateToBottomItem(it)
                        navHostFragment.navController.popBackStack(R.id.addMomentFragment, false)
                    }
                }
                R.id.navigation_setting -> {
                    if (it.itemId != binding.bottomNav.selectedItemId) {
                        navigateToBottomItem(it)
                        navHostFragment.navController.popBackStack(R.id.settingFragment, false)
                    }
                }
                R.id.navigation_profile -> {
                    if (it.itemId != binding.bottomNav.selectedItemId) {
                        navigateToBottomItem(it)
                        navHostFragment.navController.popBackStack(R.id.profileFragment, false)
                    }
                }

            }
            true
        }
    }

    private fun navigateToBottomItem(menuItem: MenuItem) {
        try {
            NavigationUI.onNavDestinationSelected(
                menuItem,
                Navigation.findNavController(this, R.id.nav_host_fragment)
            )
        } catch (e: Exception) {
            println("Exception: $e")
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
    }

    override fun setBottomNavigationVisibility(visibility: Int) {
        binding.bottomNav.visibility = visibility
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