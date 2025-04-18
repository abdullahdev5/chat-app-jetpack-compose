package com.android.chatappcompose.settings.chats.data.repository

import android.graphics.Bitmap
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.constants.CHAT_WALLPAPERS_COL
import com.android.chatappcompose.core.domain.constants.SETTINGS_CHATS_DOC
import com.android.chatappcompose.core.domain.constants.SETTINGS_COL
import com.android.chatappcompose.core.domain.constants.USERS_COL
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.settings.chats.domain.repository.SettingsChatsRepository
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class SettingsChatsRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
): SettingsChatsRepository {

    override val userDocRef: DocumentReference?
        get() = firestore.collection(USERS_COL).document(auth.currentUser?.uid.toString())

    override val settingsColRef: CollectionReference?
        get() = userDocRef?.collection(SETTINGS_COL)

    override val storageRef: StorageReference?
        get() = storage.reference



    override fun creatingSettingsChatsDocIfEmpty() {
        userDocRef
            ?.collection(SETTINGS_COL)
            ?.document(SETTINGS_CHATS_DOC)
            ?.addSnapshotListener { value, error ->
                if (!value!!.exists()) {
                    val settingsChatsModel = SettingsChatsModel("", "")
                    userDocRef
                        ?.collection(SETTINGS_COL)
                        ?.document(SETTINGS_CHATS_DOC)
                        ?.set(settingsChatsModel)
                }
            }
    }

    override fun getChatsSettings(): Flow<ResultState<SettingsChatsModel?>> = callbackFlow {
        settingsColRef
            ?.document(SETTINGS_CHATS_DOC)
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    val chatsSettingsData = value.toObject(SettingsChatsModel::class.java)
                    trySend(ResultState.Success(chatsSettingsData))
                }
            }

        awaitClose {
            close()
        }
    }

    override fun onChatWallpaperChanged(chatWallpaperBitmap: Bitmap): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)

            val chatWallpaperRef = storageRef
                ?.child(CHAT_WALLPAPERS_COL)
                ?.child(auth.currentUser?.uid.toString())
            // ?.child("${chatWallpaperBitmap}")

            // baos means ByteArrayOutputStream
            val baos = ByteArrayOutputStream()
            chatWallpaperBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte = baos.toByteArray()

            val uploadTask = chatWallpaperRef?.putBytes(imageInByte)

            uploadTask
                ?.addOnSuccessListener { document ->
                    val url = document.storage.downloadUrl

                    url
                        .addOnSuccessListener { uri ->
                            settingsColRef
                                ?.document(SETTINGS_CHATS_DOC)
                                ?.update("chatWallpaper", uri.toString())
                                ?.addOnSuccessListener {
                                    trySend(ResultState.Success("Wallpaper Changed"))
                                }
                        }
                        .addOnFailureListener {
                            trySend(ResultState.Failure(error("Failed to Change the Wallpaper. May be cause of Network Error")))
                        }

                }
                ?.addOnProgressListener { snapshot ->
                    val progress = 1.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                    trySend(ResultState.Progress(progress.toFloat()))
                }
                ?.addOnFailureListener {
                    trySend(ResultState.Failure(error("Failed to Change the Wallpaper. May be cause of Network Error")))
                }


            awaitClose() {
                close()
            }
        }


    override fun onChatWallpaperSetAsDefault(): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)

            try {

                val wallpaperStorageCol = storageRef?.child("${CHAT_WALLPAPERS_COL}")
                    ?.child("${auth.currentUser?.uid.toString()}")

                wallpaperStorageCol
                    ?.delete()
                    ?.addOnSuccessListener {
                        settingsColRef
                            ?.document(SETTINGS_CHATS_DOC)
                            ?.update("chatWallpaper", null)
                            ?.addOnSuccessListener {
                                trySend(ResultState.Success("Wallpaper Set as Default"))
                            }
                    }
                    ?.addOnFailureListener {
                        trySend(ResultState.Failure(error("Failed to Remove the Wallpaper. May be cause of Network Error")))
                    }

            } catch (e: Exception) {
                trySend(ResultState.Failure(e))
            }


            awaitClose {
                close()
            }
        }

    override fun setFontSizeToDefaultInInitOfViewModel() {
        settingsColRef
            ?.document(SETTINGS_CHATS_DOC)
            ?.addSnapshotListener { value, error ->
                if (value != null) {
                    if (value.getString("fontSize").isNullOrEmpty()) {
                        settingsColRef
                            ?.document(SETTINGS_CHATS_DOC)
                            ?.update("fontSize", "Medium")
                    }
                }
            }
    }

    override fun updateFontSize(fontSize: String) {
        settingsColRef
            ?.document(SETTINGS_CHATS_DOC)
            ?.update("fontSize", fontSize)
    }


}