@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.main.chats.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android.chatappcompose.MainDest
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.TextFieldColors
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UpdateCHatScreen(
    navHostController: NavHostController,
    args: MainDest.UPDATE_CHAT_SCREEN,
    chatViewModel: ChatViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        chatViewModel.getChatByChatId(chatId = args.chatId)
    }

    val chat by chatViewModel.chatById.collectAsStateWithLifecycle()
    var contactName by remember {
        mutableStateOf(
            if (args?.usernameGivenByChatCreator.toString().isEmpty())
                args.username
            else
                args?.usernameGivenByChatCreator?:""
        )
    }
    var isChatUpdated by remember { mutableStateOf(true) } // true By Default

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Update Chat")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back Icon"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        scope.launch(Dispatchers.Main) {
                            chatViewModel.updateChat(
                                chatId = args.chatId,
                                contactName = contactName,
                            ).collect { result ->
                                when (result) {
                                    is ResultState.Failure -> {
                                        snackbarHostState.showSnackbar(
                                            message = result.error.message.toString(),
                                            duration = SnackbarDuration.Short
                                        )
                                        isChatUpdated = true
                                    }

                                    ResultState.Loading -> {
                                        isChatUpdated = false
                                    }

                                    is ResultState.Success -> {
                                        snackbarHostState.showSnackbar(
                                            message = result.data,
                                            duration = SnackbarDuration.Short
                                        )
                                        navHostController.navigate(MainDest.MAIN_SCREEN) {
                                            popUpTo(MainDest.MAIN_SCREEN) { inclusive = true }
                                        }
                                        isChatUpdated = true
                                    }

                                    is ResultState.Progress -> TODO()
                                }
                            }
                        }
                    }) {
                    Text(text = "Update", color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                value = contactName,
                onValueChange = {
                    contactName = it
                },
                colors = TextFieldColors(),
                label = {
                    Text(text = "Contact Name")
                },
                keyboardOptions = KeyboardOptions.Default,
                singleLine = true
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                value = chat?.phoneNumber?:"",
                onValueChange = {  },
                colors = TextFieldColors(),
                label = {
                    Text(text = "Contact Phone Number")
                },
                readOnly = true,
                keyboardOptions = KeyboardOptions.Default
            )

            if (!isChatUpdated) {
                LoadingDialog()
            }

        }
    }
}