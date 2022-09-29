package com.hushbunny.app.ui.landing

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.Api
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.hushbunny.app.HomeActivity
import com.hushbunny.app.databinding.ActivitySplashBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.ui.onboarding.OnBoardingActivity
import com.hushbunny.app.uitls.APIConstants
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
        PrefsManager.get().saveBooleanValues(AppConstants.IS_REQUIRED_UPDATE_TOKEN, true)
        if (intent == null)
            navigateToNextScreen()
        else if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE)
            handleMediaFromIntent()
        else
            handleDynamicLink()
    }

    private fun handlePushNotification() {
        navigateToNextScreen(
            notificationType = intent.getStringExtra(AppConstants.NOTIFICATION_TYPE).orEmpty(),
            momentID = intent.getStringExtra(APIConstants.QUERY_PARAMS_MOMENT_ID).orEmpty(),
            kidID = intent.getStringExtra(APIConstants.QUERY_PARAMS_KID_ID).orEmpty()
        )
    }

    private fun handleMediaFromIntent() {
        navigateToNextScreen(clipData = intent.clipData, isMediaData = true)
    }

    private fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                val momentID = deepLink?.getQueryParameter(APIConstants.QUERY_PARAMS_MOMENT_ID).orEmpty()
                if (momentID.isNotEmpty())
                    navigateToNextScreen(momentID)
                else if (intent != null)
                    handlePushNotification()
                else navigateToNextScreen(momentID)
            }
            .addOnFailureListener(this) {
                if (intent != null)
                    handlePushNotification()
                else navigateToNextScreen()
            }
    }

    private fun navigateToNextScreen(
        momentID: String = "",
        kidID: String = "",
        clipData: ClipData? = null,
        isMediaData: Boolean = false,
        notificationType: String = ""
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (PrefsManager.get().getBoolean(AppConstants.IS_USER_LOGGED_IN, false)) {
                    navigateToHomePage(
                        momentID = momentID,
                        kidID = kidID,
                        clipData = clipData,
                        isMediaData = isMediaData,
                        notificationType = notificationType
                    )
                } else {
                    navigateToLoginPage()
                }
            }
        }
    }

    private suspend fun navigateToHomePage(
        momentID: String,
        kidID: String,
        isMediaData: Boolean,
        clipData: ClipData?,
        notificationType: String
    ) {
        delay(AppConstants.SPLASH_DELAY_DURATION)
        val intent = Intent(this, HomeActivity::class.java)
        if (notificationType.isNotEmpty()) {
            PrefsManager.get().saveBooleanValues(AppConstants.IS_FROM_PUSH_NOTIFICATION, true)
        } else {
            if (momentID.isNotEmpty())
                PrefsManager.get().saveBooleanValues(AppConstants.IS_REQUIRED_NAVIGATION, true)
            if (isMediaData)
                PrefsManager.get().saveBooleanValues(AppConstants.GALLERY_MEDIA, true)
            intent.clipData = clipData
        }
        intent.putExtra(AppConstants.NOTIFICATION_TYPE, notificationType)
        intent.putExtra(APIConstants.QUERY_PARAMS_MOMENT_ID, momentID)
        intent.putExtra(APIConstants.QUERY_PARAMS_KID_ID, kidID)
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