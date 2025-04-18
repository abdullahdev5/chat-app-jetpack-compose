package com.android.chatappcompose.profile.domain.repository

import android.graphics.Bitmap
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    val userDocRef: DocumentReference?

    val storageRef: StorageReference?

    fun updateUsername(
        username: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )
    fun updateUserProfilePic(userImageBitmap: Bitmap): Flow<ResultState<String>>
}