@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.main.chats.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.android.chatappcompose.MainDest
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.ui.commomComposables.ButtonColors
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.TextFieldColors
import com.android.chatappcompose.main.chats.domain.constants.countryCodeList
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddChatScreen(
    navHostController: NavHostController,
    chatViewModel: ChatViewModel,
) {

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var number by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf(countryCodeList[22]) }
    val contactPhoneNumber by remember { derivedStateOf { countryCode + number } }
    var contactName by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    var isContactAdded by remember { mutableStateOf(true) } // it;s true by default

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopBar(navHostController = navHostController)
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonColors(),
                    enabled = if (number.isEmpty()) false else true,
                    onClick = {
                        scope.launch(Dispatchers.Main) {
                            chatViewModel.addChat(contactPhoneNumber, contactName)
                                .collect {
                                    when (it) {
                                        is ResultState.Failure -> {
                                            Toast.makeText(
                                                context,
                                                "${it.error}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isContactAdded = true
                                        }

                                        ResultState.Loading -> {
                                            isContactAdded = false
                                        }

                                        is ResultState.Success -> {
                                            Toast.makeText(
                                                context,
                                                "${it.data}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isContactAdded = true
                                            number = ""
                                            contactName = ""
                                            navHostController.navigate(MainDest.MAIN_SCREEN) {
                                                popUpTo(MainDest.MAIN_SCREEN) { inclusive = true }
                                            }
                                        }

                                        is ResultState.Progress -> TODO()
                                    }
                                }
                        }
                    }
                ) {
                    Text(text = "Add Contact")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .verticalScroll(scrollState)
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp),
                colors = TextFieldColors(),
                value = contactName,
                onValueChange = {
                    contactName = it
                },
                label = {
                    Text(
                        text = "Contact Name (Optional)",
                        style = TextStyle(
                            fontSize = 12.sp
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person Icon for Contact Name"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp)
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Country Code Text Field
                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(53.dp)
                        .padding(end = 5.dp),
                    expanded = isExpanded,
                    onExpandedChange = {
                        isExpanded = !isExpanded
                    }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor()
                            .width(120.dp)
                            .height(53.dp),
                        colors = TextFieldColors(),
                        value = countryCode,
                        readOnly = true,
                        onValueChange = {  },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        countryCodeList.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = item)
                                },
                                onClick = {
                                    countryCode = countryCodeList[index]
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                // Phone Number Text Field
                TextField(
                    modifier = Modifier
                        .weight(1f)
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
                        Text(
                            text = "Contact Number",
                            style = TextStyle(
                                fontSize = 12.sp
                            )
                        )
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

            if (!isContactAdded) {
                LoadingDialog()
            }

        }
    }
}

@Composable
fun TopBar(
    navHostController: NavHostController
) {
    TopAppBar(
        title = {
            Text(text = "Add Contact")
        },
        navigationIcon = {
            IconButton(onClick = {
                navHostController.navigateUp()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Icon that navigates back"
                )
            }
        }
    )
}