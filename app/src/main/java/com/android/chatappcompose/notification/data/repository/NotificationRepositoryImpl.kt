package com.android.chatappcompose.notification.data.repository

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.android.chatappcompose.MainActivity
import com.android.chatappcompose.notification.data.module.SimpleNotificationCompatBuilder
import com.android.chatappcompose.notification.data.module.SendingMediaNotificationCompatBuilder
import com.android.chatappcompose.notification.domain.constants.NotificationConstants
import com.android.chatappcompose.notification.domain.repository.NotificationRepository
import javax.inject.Inject
import kotlin.math.roundToInt

class NotificationRepositoryImpl @Inject constructor(
    @SimpleNotificationCompatBuilder
    private val simpleNotificationBuilder: NotificationCompat.Builder,
    @SendingMediaNotificationCompatBuilder
    private val sendingMediaNotificationBuilder: NotificationCompat.Builder,
    private val notificationManager: NotificationManagerCompat
) : NotificationRepository {

    override fun onImageMessageSendNotificationWithProgress(
        context: Context,
        progress: Float,
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(
            NotificationConstants.SENDING_MEDIA_NOTIFICATION_ID,
            sendingMediaNotificationBuilder
                .setContentTitle("Image Sending... please don't close the app")
                .setContentText("${(progress * 100).roundToInt()}%")
                .setProgress(100, (progress * 100).roundToInt(), false)
                .build()
        )
    }

    override fun cancelProgressNotification() {
        notificationManager.cancel(NotificationConstants.SENDING_MEDIA_NOTIFICATION_ID)
    }


    override fun notifySimpleNotificationForMessage(
        context: Context,
        chatId: String,
        title: String,
        text: String
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE
            else
                0

        // This Code is For Open the Screen Not Activity ignore this Now We Created navDeepLink in the NavGraph then You Learn More.
        val clickedIntent = Intent(
            Intent.ACTION_VIEW,
            "$MY_URI/chatId=$chatId".toUri(), // And this MY_URI Launch the Screen Where You Add navDeeplink and Add Add UriPattern = MY_URI. And I Pass the Message Also Like if Comming From MainScreen or Comming From Notification.
            context,
            MainActivity::class.java
        )
        val clickedPendingIntent: PendingIntent =
            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(clickedIntent)
                getPendingIntent(1, flag)!!
            }

        notificationManager.notify(
            NotificationConstants.SIMPLE_NOTIFICATION_ID,
            simpleNotificationBuilder
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(clickedPendingIntent)
                .build()
        )
    }

    override fun onNotifySimpleNotification(
        context: Context,
        title: String,
        text: String
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        // This Code Notify the Notification
        notificationManager.notify(
            NotificationConstants.SIMPLE_NOTIFICATION_ID, // this Define's the Notification
            simpleNotificationBuilder
                .setContentTitle(title)
                .setContentText(text)
                .build()
        )
    }

    override fun cancelSimpleNotification() {
        notificationManager.cancel(NotificationConstants.SIMPLE_NOTIFICATION_ID)
    }


}

const val MY_URI = "https://www.chatApp.com"