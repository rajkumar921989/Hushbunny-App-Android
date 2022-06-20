package com.hushbunny.app.ui.landing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hushbunny.app.HomeActivity
import com.hushbunny.app.databinding.ActivitySplashBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.ui.onboarding.OnBoardingActivity
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        (application as AppComponentProvider).getAppComponent().inject(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (PrefsManager.get().getBoolean(AppConstants.IS_USER_LOGGED_IN, false)) {
                    navigateToHomePage()
                } else {
                    navigateToLoginPage()
                }
            }
        }
    }

    private suspend fun navigateToHomePage() {
        delay(AppConstants.SPLASH_DELAY_DURATION)
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        withContext(Dispatchers.Main) {
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private suspend fun navigateToLoginPage() {
        delay(AppConstants.SPLASH_DELAY_DURATION)
        val intent = Intent(this, OnBoardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        withContext(Dispatchers.Main) {
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}