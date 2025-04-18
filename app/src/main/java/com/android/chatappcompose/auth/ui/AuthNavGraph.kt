package com.android.chatappcompose.auth.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.android.chatappcompose.auth.domain.constants.GET_PHONENUMBER_SCREEN
import com.android.chatappcompose.auth.domain.constants.GET_USERNAME_SCREEN
import com.android.chatappcompose.auth.domain.constants.OTP_SCREEN
import com.android.chatappcompose.auth.ui.screens.GetPhoneNumberScreen
import com.android.chatappcompose.auth.ui.screens.GetUserNameScreen
import com.android.chatappcompose.auth.ui.screens.OTPScreen
import com.android.chatappcompose.auth.ui.viewModel.AuthViewModel

@Composable
fun AuthNavGraph(
    navHostController: NavHostController,
    activity: Activity,
    authViewModel: AuthViewModel,
    onUserStored: () -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = GET_PHONENUMBER_SCREEN
    ) {
        composable<GET_USERNAME_SCREEN>() {
            GetUserNameScreen(
                onUserStored = onUserStored,
                authViewModel = authViewModel,
            )
        }
        composable<GET_PHONENUMBER_SCREEN>() {
            GetPhoneNumberScreen(
                navHostController = navHostController,
                activity = activity,
                authViewModel = authViewModel
            )
        }
        composable<OTP_SCREEN>() {
            OTPScreen(
                navHostController = navHostController,
                activity = activity,
                authViewModel = authViewModel,
            )
        }
    }
}