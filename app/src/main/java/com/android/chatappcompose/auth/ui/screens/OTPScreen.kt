package com.android.chatappcompose.auth.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android.chatappcompose.MainActivity
import com.android.chatappcompose.auth.domain.constants.GET_USERNAME_SCREEN
import com.android.chatappcompose.auth.domain.constants.OTP_SCREEN
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.auth.ui.viewModel.AuthViewModel
import com.android.chatappcompose.core.ui.commomComposables.ButtonColors
import com.android.chatappcompose.core.ui.commomComposables.ErrorDialog
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.OtpView
import com.android.chatappcompose.ui.theme.Green
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OTPScreen(
    navHostController: NavHostController,
    activity: Activity,
    authViewModel: AuthViewModel,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var otpValue by remember { mutableStateOf("") }
    var isUserVerified by remember { mutableStateOf(true) } // By Default its True
    var isErrorDialogOpen by remember { mutableStateOf(false) } // By Default its False
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Text(
            modifier = Modifier
                .padding(all = 30.dp)
                .align(Alignment.CenterHorizontally),
            text = "OTP Verification",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        Text(
            modifier = Modifier
                .padding(all = 20.dp)
                .align(Alignment.CenterHorizontally),
            text = "OTP Will be Sending to Your SMS\n\t in Few Seconds Please Wait."
        )

        OtpView(otpLength = 6, otpValue = otpValue) { newOtp ->
            otpValue = newOtp // Update the OTP value in the parent
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp),
            colors = ButtonColors(),
            onClick = {
                if (otpValue.isNotEmpty()) {
                    scope.launch(Dispatchers.Main) {
                        authViewModel.SigninWithCredential(
                            otp = otpValue
                        ).collect {
                            when (it) {
                                is ResultState.Success -> {
                                    Toast.makeText(context, "${it.data}", Toast.LENGTH_SHORT).show()
                                    authViewModel
                                        .StoreUser()
                                        .collect {
                                            when (it) {
                                                is ResultState.Failure -> {
                                                    isUserVerified = true
                                                    isErrorDialogOpen = true
                                                    errorMsg = it.error.message.toString()
                                                }

                                                ResultState.Loading -> {
                                                    isUserVerified = false
                                                }

                                                is ResultState.Success -> {
                                                    navHostController.navigate(GET_USERNAME_SCREEN) {
                                                        popUpTo(GET_USERNAME_SCREEN) {
                                                            inclusive = true
                                                        }
                                                    }
                                                }

                                                is ResultState.Progress -> TODO()
                                            }
                                        }
                                }

                                is ResultState.Failure -> {
                                    isUserVerified = true
                                    isErrorDialogOpen = true
                                    errorMsg = it.error.message.toString()
                                }

                                is ResultState.Loading -> {
                                    isUserVerified = false
                                }

                                is ResultState.Progress -> TODO()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "OTP Value can't be empty", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = "Verify")
        }

        if (!isUserVerified) {
            LoadingDialog()
        }
        if (isErrorDialogOpen) {
            ErrorDialog(
                dialogTitle = "Error",
                dialogText = errorMsg,
                icon = Icons.Default.Warning,
                onDismissRequest = { isErrorDialogOpen = false },
            )
        }
    }
}