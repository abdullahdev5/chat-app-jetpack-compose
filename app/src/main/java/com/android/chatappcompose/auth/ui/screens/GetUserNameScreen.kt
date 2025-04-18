@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.auth.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.android.chatappcompose.auth.domain.constants.GET_PHONENUMBER_SCREEN
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.auth.ui.viewModel.AuthViewModel
import com.android.chatappcompose.core.ui.commomComposables.ButtonColors
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.TextFieldColors
import com.android.chatappcompose.ui.theme.Green
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GetUserNameScreen(
    onUserStored: () -> Unit,
    authViewModel: AuthViewModel
) {

    val scrollState = rememberScrollState()
    var scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var isUserStored by remember { mutableStateOf(false) }

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
                text = "Add Your Full Name",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                colors = TextFieldColors(),
                value = username,
                onValueChange = {
                    username = it
                },
                label = {
                    Text(
                        style = TextStyle(
                            fontSize = 15.sp
                        ),
                        text = "Full Name (You Can Change it After)"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person Icon for Username"
                    )
                },
                singleLine = true
            )

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
                text = "This Name Will be Visible to You and Your Friends. and You can Also Change Your Name From Settings and Profile."
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                colors = ButtonColors(),
                enabled = if (username.isNotEmpty()) true else false,
                onClick = {
                    scope.launch(Dispatchers.Main) {
                        authViewModel.UpdateUser(
                            username = username,
                        ).collect {
                            when (it) {
                                is ResultState.Failure -> {
                                    isUserStored = true
                                }

                                ResultState.Loading -> {
                                    isUserStored = false
                                }

                                is ResultState.Success -> {
                                    isUserStored = true
                                    onUserStored()
                                }

                                is ResultState.Progress -> TODO()
                            }
                        }
                    }
                }) {
                Text(text = "Next")
            }

            if (isUserStored) {
                LoadingDialog()
            }

        }
    }
}