package com.android.chatappcompose.notification.domain.repository

import android.content.Context

interface NotificationRepository {

    // Sending Media Notification
    fun onImageMessageSendNotificationWithProgress(
        context: Context,
        progress: Float,
    )
    fun cancelProgressNotification()
    // Other Notification
    fun notifySimpleNotificationForMessage(
        context: Context,
        chatId: String,
        title: String,
        text: String,
    )
    fun onNotifySimpleNotification(
        context: Context,
        title: String,
        text: String
    )
    fun cancelSimpleNotification()

}