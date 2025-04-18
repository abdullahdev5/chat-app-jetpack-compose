package com.android.chatappcompose.auth.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.android.chatappcompose.auth.domain.constants.GET_PHONENUMBER_SCREEN
import com.android.chatappcompose.auth.domain.constants.OTP_SCREEN
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.auth.ui.viewModel.AuthViewModel
import com.android.chatappcompose.core.ui.commomComposables.ButtonColors
import com.android.chatappcompose.core.ui.commomComposables.ErrorDialog
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.TextFieldColors
import com.android.chatappcompose.ui.theme.Green
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@Composable
fun GetPhoneNumberScreen(
    navHostController: NavHostController,
    activity: Activity,
    authViewModel: AuthViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var number by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+92") }
    val phoneNumber = countryCode + number
    var isUserCreated by remember { mutableStateOf(true) } // By Default its True
    var isErrorDialogOpen by remember { mutableStateOf(false) } // By Default its False
    var errorMsg by remember { mutableStateOf("") }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues = innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(all = 20.dp),
                text = "Add Your Your Phone Number",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Country Code Text Field
                OutlinedTextField(
                    modifier = Modifier
                        .width(100.dp)
                        .height(53.dp)
                        .padding(start = 10.dp, end = 5.dp),
                    colors = TextFieldColors(),
                    value = countryCode,
                    onValueChange = {
                        countryCode = if (countryCode.length < 7) {
                            it
                        } else {
                            if (it.length < countryCode.length) {
                                it
                            } else {
                                countryCode
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )
                )

                // Number Text Field
                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 5.dp),
                    colors = TextFieldColors(),
                    value = number,
                    onValueChange = {
                        number = if (number.length < 15) {
                            it
                        } else {
                            if (it.length < number.length) {
                                it
                            } else {
                                number
                            }
                        }
                    },
                    label = {
                        Text(text = "Phone Number")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon for Phone Number"
                        )
                    }
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                text = "Note:-",
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic
                ),
                text = "With This Phone Number Your Friends can make Contact with You."
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                colors = ButtonColors(),
                enabled =
                if (number.length <= 15 && number.length != 0 && !countryCode.endsWith("+"))
                    true
                else
                    false,
                onClick = {
                    scope.launch(Dispatchers.Main) {
                        authViewModel.CreateUser(
                            phoneNumber = phoneNumber,
                            activity = activity
                        ).collect {
                            when (it) {
                                is ResultState.Success -> {
                                    isUserCreated = true
                                    Toast.makeText(context, "${it.data}", Toast.LENGTH_SHORT).show()
                                    navHostController.navigate(OTP_SCREEN)
                                }

                                is ResultState.Failure -> {
                                    isUserCreated = true
                                    isErrorDialogOpen = true
                                    errorMsg = it.error.message.toString()
                                }

                                is ResultState.Loading -> {
                                    isUserCreated = false
                                }

                                is ResultState.Progress -> TODO()
                            }
                        }
                    }
                }) {
                Text(text = "Next")
            }
            if (!isUserCreated) {
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
}