package com.android.chatappcompose.settings.chats.domain.repository

import android.graphics.Bitmap
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow


interface SettingsChatsRepository {

    val userDocRef: DocumentReference?
    val settingsColRef: CollectionReference?
    val storageRef: StorageReference?
    fun creatingSettingsChatsDocIfEmpty()
    fun getChatsSettings(): Flow<ResultState<SettingsChatsModel?>>
    fun onChatWallpaperChanged(chatWallpaperBitmap: Bitmap): Flow<ResultState<String>>
    fun onChatWallpaperSetAsDefault(): Flow<ResultState<String>>
    fun setFontSizeToDefaultInInitOfViewModel()
    fun updateFontSize(fontSize: String)

}