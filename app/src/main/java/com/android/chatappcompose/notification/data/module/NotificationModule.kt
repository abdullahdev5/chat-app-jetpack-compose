package com.android.chatappcompose.notification.data.module

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.chatappcompose.notification.domain.constants.NotificationConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Qualifier


@Module
@InstallIn(ViewModelComponent::class)
class NotificationModule {

    @Provides
    @SimpleNotificationCompatBuilder
    fun ProvideSimpleNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationConstants.OTHER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    @Provides
    @SendingMediaNotificationCompatBuilder
    fun ProvideSendingMediaNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationConstants.SENDING_MEDIA_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
    }



    @Provides
    fun ProvideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationConstants.OTHER_NOTIFICATION_CHANNEL_ID, // THis Defines the Notification Channel
                NotificationConstants.OTHER_NOTIFICATION_CHANNEL_NAME, // This Will be Visible to the User
                NotificationManager.IMPORTANCE_HIGH
            )
            val sendingMediaChannel = NotificationChannel(
                NotificationConstants.SENDING_MEDIA_NOTIFICATION_CHANNEL_ID, // THis Defines the Notification Channel
                NotificationConstants.SENDING_MEDIA_NOTIFICATION_CHANNEL_NAME, // This Will be Visible to the User
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(channel)
            // Channel 2
            notificationManager.createNotificationChannel(sendingMediaChannel)
        }
        return notificationManager
    }



}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SimpleNotificationCompatBuilder

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SendingMediaNotificationCompatBuilder