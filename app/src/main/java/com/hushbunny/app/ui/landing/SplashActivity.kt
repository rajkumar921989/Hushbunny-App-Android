package com.hushbunny.app.ui.landing

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.hushbunny.app.databinding.ActivitySplashBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.ui.onboarding.OnBoardingActivity

class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        (application as AppComponentProvider).getAppComponent().inject(this)
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLoginPage()
        }, 4000)
    }


    private fun navigateToLoginPage() {
        val intent = Intent(this, OnBoardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}