package com.android.chatappcompose.auth.data.module

import com.android.chatappcompose.auth.data.repository.AuthRepositoryImpl
import com.android.chatappcompose.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class AuthModule {

    @Provides
    fun provideAuthRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(auth, fireStore)
    }

}