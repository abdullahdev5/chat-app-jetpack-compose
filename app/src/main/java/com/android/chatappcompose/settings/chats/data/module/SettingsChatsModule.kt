package com.android.chatappcompose.settings.chats.data.module

import com.android.chatappcompose.settings.chats.data.repository.SettingsChatsRepositoryImpl
import com.android.chatappcompose.settings.chats.domain.repository.SettingsChatsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent


@Module
@InstallIn(ViewModelComponent::class)
class SettingsChatsModule {

    @Provides
    fun provideSettingsChatsRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        storage: FirebaseStorage
    ): SettingsChatsRepository {
        return SettingsChatsRepositoryImpl(auth, fireStore, storage)
    }

}