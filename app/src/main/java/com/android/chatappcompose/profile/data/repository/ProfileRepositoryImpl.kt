package com.android.chatappcompose.profile.data.repository

import android.graphics.Bitmap
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.constants.USERS_COL
import com.android.chatappcompose.core.domain.constants.USER_IMAGES_COL
import com.android.chatappcompose.profile.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage

) : ProfileRepository {

    override val userDocRef: DocumentReference?
        get() = fireStore.collection(USERS_COL).document(auth.currentUser?.uid.toString())

    override val storageRef: StorageReference?
        get() = storage.reference

    override fun updateUsername(
        username: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        userDocRef
            ?.update("username", username)
            ?.addOnSuccessListener {
                onSuccess.invoke("name updated")
            }
            ?.addOnFailureListener {
                onFailure.invoke("Failed to update name")
            }
    }

    override fun updateUserProfilePic(userImageBitmap: Bitmap): Flow<ResultState<String>> =
        callbackFlow {

            trySend(ResultState.Loading)

            val userImageRef =
                storageRef?.child(USER_IMAGES_COL)
                    ?.child(" ${auth.currentUser?.uid.toString()} ${userImageBitmap}")

            // baos means ByteArrayOutputStream
            val baos = ByteArrayOutputStream()
            userImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte = baos.toByteArray()

            val uploadTask = userImageRef?.putBytes(imageInByte)

            uploadTask
                ?.addOnSuccessListener { document ->
                    val url = document.storage.downloadUrl

                    url
                        .addOnSuccessListener { uri ->
                            userDocRef
                                ?.update("profilePic", uri.toString())
                                ?.addOnSuccessListener {
                                    trySend(ResultState.Success("profile pic updated"))
                                }
                        }
                        .addOnFailureListener {
                            trySend(ResultState.Failure(error("Failed to update profile pic. May be cause of Network Error")))
                        }

                }
                ?.addOnProgressListener { snapshot ->
                    val progress = 1.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                    trySend(ResultState.Progress(progress.toFloat()))
                }
                ?.addOnFailureListener {
                    trySend(ResultState.Failure(error("Failed to Update Profile Pic. May be cause of Network Error")))
                }


            awaitClose {
                close()
            }
        }
}