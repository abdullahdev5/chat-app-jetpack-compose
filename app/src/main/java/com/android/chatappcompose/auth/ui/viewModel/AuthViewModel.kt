package com.android.chatappcompose.auth.ui.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.chatappcompose.auth.domain.repository.AuthRepository
import com.android.chatappcompose.core.domain.model.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
): ViewModel() {

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun CreateUser(
        phoneNumber: String,
        activity: Activity
    ) = authRepository.CreateUser(phoneNumber, activity)

    fun SigninWithCredential(
        otp: String
    ) = authRepository.SigninWithCredential(otp)

    fun SignOutUser() = authRepository.SignOutUser()

    fun StoreUser() = authRepository.StoreUser(user = UserModel(
        username = "User",
        phoneNumber = currentUser?.phoneNumber.toString(),
        profilePic = null,
        key = currentUser?.uid.toString(),
        timeStamp = Timestamp.now()
    ))

    fun UpdateUser(
        username : String
    ) = authRepository.UpdateUsername(username)

}