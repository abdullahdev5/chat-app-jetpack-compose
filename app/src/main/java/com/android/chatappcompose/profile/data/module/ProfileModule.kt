package com.android.chatappcompose.profile.data.module

import com.android.chatappcompose.profile.data.repository.ProfileRepositoryImpl
import com.android.chatappcompose.profile.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ProfileModule {

    @Provides
    fun provideProfileRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ProfileRepository {
        return ProfileRepositoryImpl(auth, fireStore, storage)
    }

}