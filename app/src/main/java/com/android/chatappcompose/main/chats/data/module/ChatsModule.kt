package com.android.chatappcompose.main.chats.data.module

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.chatappcompose.main.chats.data.repository.ChatRepositoryImpl
import com.android.chatappcompose.main.chats.domain.repository.ChatRepository
import com.android.chatappcompose.notification.data.module.SimpleNotificationCompatBuilder
import com.android.chatappcompose.notification.data.module.SendingMediaNotificationCompatBuilder
import com.android.chatappcompose.notification.data.repository.NotificationRepositoryImpl
import com.android.chatappcompose.notification.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ChatsModule {

    @Provides
    fun provideChatRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ChatRepository {
        return ChatRepositoryImpl(auth, fireStore, storage)
    }

    @Provides
    fun provideNotificationRepository(
        @SimpleNotificationCompatBuilder
        mainNotificationBuilder: NotificationCompat.Builder,
        @SendingMediaNotificationCompatBuilder
        secondNotificationBuilder: NotificationCompat.Builder,
        notificationManager: NotificationManagerCompat
    ): NotificationRepository {
        return NotificationRepositoryImpl(mainNotificationBuilder, secondNotificationBuilder, notificationManager)
    }

}