package com.android.chatappcompose.auth.data.repository

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.android.chatappcompose.auth.domain.repository.AuthRepository
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.constants.USERS_COL
import com.android.chatappcompose.core.domain.model.UserModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) : AuthRepository {

    private lateinit var onVerificationCode: String


    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun CreateUser(
        phoneNumber: String,
        activity: Activity
    ): Flow<ResultState<String>> = callbackFlow {

        trySend(ResultState.Loading)

        val onVerificationCallback = object  : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                TODO("Not yet implemented")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                trySend(ResultState.Failure(p0))
            }

            override fun onCodeSent(verificationCode: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationCode, p1)
                trySend(ResultState.Success("OTP Will be Send to Your SMS in Few Seconds"))
                onVerificationCode = verificationCode
            }

        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(onVerificationCallback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose {
            close()
        }
    }

    override fun SigninWithCredential(otpCode: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        val credential = PhoneAuthProvider.getCredential(onVerificationCode, otpCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(ResultState.Success("OTP Verified Successfully"))
                } else {
                    trySend(ResultState.Failure(task.exception!!))
                }
            }
        awaitClose {
            close()
        }
    }

    override fun SignOutUser() {
        auth.signOut()
    }

    override fun StoreUser(user: UserModel): Flow<ResultState<UserModel>> = callbackFlow {
        trySend(ResultState.Loading)
            fireStore
                .collection(USERS_COL)
                .document(user.key)
                .set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        trySend(ResultState.Success(user))
                    } else {
                        trySend(ResultState.Failure(it.exception!!))
                    }
                }
        awaitClose {
            close()
        }
    }

    override fun UpdateUsername(username: String): Flow<ResultState<String>> = callbackFlow{
        trySend(ResultState.Loading)
        fireStore
            .collection(USERS_COL)
            .document(currentUser?.uid.toString())
            .update("username", username)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    trySend(ResultState.Success(""))
                } else {
                    trySend(ResultState.Failure(it.exception!!))
                }
            }
        awaitClose {
            close()
        }
    }

}