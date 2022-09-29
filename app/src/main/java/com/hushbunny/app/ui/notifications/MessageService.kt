package com.hushbunny.app.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hushbunny.app.R
import com.hushbunny.app.ui.landing.SplashActivity
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.PrefsManager
import org.json.JSONObject
import java.util.*

class MessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("onMessageReceived data--${remoteMessage.data}")
        handleNotification(remoteMessage.data)
    }

    private fun handleNotification(data: Map<String, String>) {
        val channelId = NOTIFICATION_CHANNEL_ID
        val notificationData = JSONObject(data)
        println("notificationData type--${notificationData.optString("type")}")
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(SplashActivity::class.java)
        val intent = Intent(this, SplashActivity::class.java)
        val requestID = Calendar.getInstance().timeInMillis.toInt()
        intent.putExtra(AppConstants.NOTIFICATION_TYPE, notificationData.optString("type"))
        intent.putExtra(APIConstants.QUERY_PARAMS_MOMENT_ID, notificationData.optString("momentId"))
        intent.putExtra(APIConstants.QUERY_PARAMS_KID_ID, notificationData.optString("kidId"))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.action = System.currentTimeMillis().toString()
        stackBuilder.addNextIntent(intent)
        val pendingIntent =
            stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_push_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(notificationData.optString("message"))
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val channel = NotificationChannel(
            channelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        println("onNewToken--$token")
        PrefsManager.get().saveStringValue(AppConstants.FIRE_BASE_TOKEN, token)
        super.onNewToken(token)
    }


    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "1000"
    }
}