@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.android.chatappcompose.MainDest
import com.android.chatappcompose.SettingsDest
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import com.android.chatappcompose.main.chats.ui.screens.ChatsScreen
import com.android.chatappcompose.main.updates.ui.screens.UpdatesScreen
import com.android.chatappcompose.ui.theme.Green
import com.android.chatappcompose.ui.theme.SearchBarColorLight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navHostController: NavHostController,
    chatViewModel: ChatViewModel,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        BottomNavItems.Chats,
        BottomNavItems.Updates
    )
    val pagerState = rememberPagerState(
        pageCount = {
            bottomNavItems.size
        }
    )

    var chatId by remember { mutableStateOf("") }

    var isSelected by remember { mutableStateOf(false) }
    var usernameGivenByChatCreator by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isChatDeleted by remember { mutableStateOf(true) } // true By Default
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isMoreDropDownMenuVisible by remember { mutableStateOf(false) }


    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            if (isSelected) {
                TopAppBar(
                    modifier = Modifier,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight
                    ),
                    title = {
                        Text(
                            text =
                            if (usernameGivenByChatCreator.isEmpty())
                                phoneNumber
                            else
                                usernameGivenByChatCreator,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelected = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isSelected = false
                            navHostController.navigate(
                                MainDest.UPDATE_CHAT_SCREEN(
                                    chatId = chatId,
                                    username = username,
                                    usernameGivenByChatCreator = usernameGivenByChatCreator
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Chat Icon"
                            )
                        }

                        IconButton(onClick = {
                            isSelected = false
                            isDeleteDialogOpen = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Chat Icon"
                            )
                        }
                    },
                )
            }
            else {
                TopAppBar(
                    title = {
                        Text(
                            text =
                            if (pagerState.currentPage == 0) "Chat App"
                            else if (pagerState.currentPage == 1) "Updates"
                            else "",
                            color = if (pagerState.currentPage == 0) Green else {
                                if (isSystemInDarkTheme()) Color.White else Color.Black
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            onLogout.invoke()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        }

                        IconButton(onClick = {
                            isMoreDropDownMenuVisible = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }

                        if (isMoreDropDownMenuVisible) {
                            MoreDropDownMenu(
                                isMoreDropDownMenuVisible = isMoreDropDownMenuVisible,
                                onSettingsClick = {
                                    navHostController.navigate(SettingsDest.SETTINGS_SCREEN)
                                },
                                onDismiss = {
                                    isMoreDropDownMenuVisible = false
                                }
                            )
                        }

                    },
                )
            }
            if (pagerState.currentPage != 0) {
                isSelected = false
            }

        },
        bottomBar = {
            Column {
                Divider()
                BottomBar(
                    bottomNavItems = bottomNavItems,
                    pagerState = pagerState,
                    scope = scope,
                    chatViewModel = chatViewModel
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                containerColor = Green,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                onClick = {
                    if (pagerState.currentPage == 0) {
                        navHostController.navigate(MainDest.ADD_CHAT_SCREEN)
                    }
                }) {
                Icon(
                    imageVector = if (pagerState.currentPage == 0)
                        Icons.Default.Add
                    else
                        Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            state = pagerState,
            beyondViewportPageCount = bottomNavItems.size
        ) { page ->
            if (page == 0) {
                ChatsScreen(
                    chatViewModel = chatViewModel,
                    navHostController = navHostController,
                    onChatLongPress = { chatID, isSelectedPassed, usernamePassed,
                                        usernameGivenByChatCreatorPassed, phoneNumberPassed ->
                        chatId = chatID
                        username = usernamePassed
                        usernameGivenByChatCreator = usernameGivenByChatCreatorPassed
                        phoneNumber = phoneNumberPassed
                        isSelected = isSelectedPassed
                    },
                    onTapIfSelected = {
                        isSelected = false
                    },
                    isSelected = isSelected
                )
            }
            if (page == 1) {
                UpdatesScreen()
            }


            if (isDeleteDialogOpen) {
                DeleteChatDialog(
                    onDismiss = {
                        isDeleteDialogOpen = false
                    },
                    onDelete = {
                        isDeleteDialogOpen = false
                        scope.launch(Dispatchers.Main) {
                            chatViewModel.deleteChat(chatId)
                                .collect { result ->
                                    when (result) {
                                        is ResultState.Failure -> {
                                            snackbarHostState.showSnackbar(
                                                message = result.error.message.toString(),
                                                duration = SnackbarDuration.Short
                                            )
                                            isChatDeleted = true
                                        }

                                        ResultState.Loading -> {
                                            isChatDeleted = false
                                        }

                                        is ResultState.Success -> {
                                            snackbarHostState.showSnackbar(
                                                message = result.data,
                                                duration = SnackbarDuration.Short
                                            )
                                            isChatDeleted = true
                                        }

                                        is ResultState.Progress -> TODO()
                                    }
                                }
                        }
                    },
                    title = if (usernameGivenByChatCreator.isEmpty()) phoneNumber else usernameGivenByChatCreator
                )
            }

            if (!isChatDeleted) {
                LoadingDialog()
            }

        }
    }
}

@Composable
fun BottomBar(
    bottomNavItems: List<BottomNavItems>,
    pagerState: PagerState,
    scope: CoroutineScope,
    chatViewModel: ChatViewModel
) {

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        containerColor = Color.Transparent,
        contentColor = Color.Black,
    ) {
        bottomNavItems.forEachIndexed { index, items ->
            NavigationBarItem(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                label = {
                    Text(text = items.title)
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (index == 0) {
                                if (items.badgeCount != null) {
                                    if (chatViewModel.totalUnreadCount.value != 0) {
                                        Badge(
                                            containerColor = Green,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = chatViewModel.totalUnreadCount.value.toString())
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (index == pagerState.currentPage) items.selectedIcon else items.unselectedIcon,
                            contentDescription = null
                        )
                    }

                },
                interactionSource = MutableInteractionSource()
            )
        }
    }
}

@Composable
fun DeleteChatDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    title: String
) {
    AlertDialog(
        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Chat Icon"
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = "This Will Delete this Chat and All the Messages in this Chat")
        },
        onDismissRequest = {
            onDismiss.invoke()
        },
        confirmButton = {
            OutlinedButton(onClick = {
                onDelete.invoke()
            }) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun MoreDropDownMenu(
    isMoreDropDownMenuVisible: Boolean,
    onSettingsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier
            .background(color = if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
        expanded = isMoreDropDownMenuVisible,
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = true
        )
    ) {
        DropdownMenuItem(
            text = {
                Text(text = "Settings")
            },
            onClick = {
                onSettingsClick.invoke()
                onDismiss.invoke()
            }
        )
    }
}