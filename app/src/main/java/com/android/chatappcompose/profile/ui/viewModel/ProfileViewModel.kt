package com.android.chatappcompose.profile.ui.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository
): ViewModel() {

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser = _currentUser.asStateFlow()



    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        profileRepo
            .userDocRef
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    _currentUser.value = value.toObject(UserModel::class.java)
                }
            }
    }

    fun updateUserProfilePic(
        image: Bitmap
    ) = profileRepo.updateUserProfilePic(image)

    fun updateUsername(
        username: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = profileRepo.updateUsername(username, onSuccess, onFailure)

}