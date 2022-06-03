package com.hushbunny.app.ui.onboarding

import android.os.Bundle
import android.text.TextUtils.replace
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ActivityOnBoardingBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.ui.onboarding.login.LoginFragment

class OnBoardingActivity : AppCompatActivity() {
    private var binding: ActivityOnBoardingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        (application as AppComponentProvider).getAppComponent().inject(this)
        supportFragmentManager.commit {
            replace(
                R.id.fragment_container_view,
                LoginFragment.getInstance(),
                LoginFragment::class.simpleName.toString()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}