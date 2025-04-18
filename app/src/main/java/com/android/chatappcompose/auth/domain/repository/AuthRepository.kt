package com.android.chatappcompose.auth.domain.repository

import android.app.Activity
import androidx.compose.runtime.MutableState
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.model.UserModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface AuthRepository {

    val currentUser: FirebaseUser?

    fun CreateUser(
        phoneNumber: String,
        activity: Activity
    ): Flow<ResultState<String>>

    fun SigninWithCredential(otpCode: String): Flow<ResultState<String>>

    fun SignOutUser()

    fun StoreUser(user: UserModel): Flow<ResultState<UserModel>>

    fun UpdateUsername(username: String): Flow<ResultState<String>>

}