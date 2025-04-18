package com.android.chatappcompose.main.chats.ui.screens

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.android.chatappcompose.MainDest
import com.android.chatappcompose.core.domain.common_function.formatTimestampToAmPm
import com.android.chatappcompose.core.domain.common_function.formatTimestampToDate
import com.android.chatappcompose.core.ui.commomComposables.FullScreenImageDialog
import com.android.chatappcompose.core.ui.commomComposables.LastMessage
import com.android.chatappcompose.main.chats.domain.model.ChatModel
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import com.android.chatappcompose.ui.theme.Green
import com.android.chatappcompose.ui.theme.LowGreenDark
import com.android.chatappcompose.ui.theme.LowGreenLight

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ChatsScreen(
    chatViewModel: ChatViewModel,
    navHostController: NavHostController,
    onChatLongPress: (String, Boolean, String, String, String) -> Unit,
    onTapIfSelected: () -> Unit,
    isSelected: Boolean
) {

    val scope = rememberCoroutineScope()

    var isChatImageDialogOpen by remember { mutableStateOf(false) }

    var isChatImageClicked by remember { mutableStateOf(false) }

    val chats = chatViewModel.chatsList.collectAsStateWithLifecycle()

    val selectedItemIndex = remember { mutableStateOf<Int?>(null) }

    val chatImageDialogScale = remember {
        Animatable(0f)
    }

    val fullChatImageScale = remember {
        Animatable(0f)
    }


    LaunchedEffect(key1 = true) {
        chatViewModel.cancelSimpleNotification()
    }

    LaunchedEffect(key1 = isChatImageDialogOpen) {
        if (isChatImageDialogOpen) {

            chatImageDialogScale.animateTo(1f)

        } else {
            chatImageDialogScale.animateTo(0f)
        }
    }

    LaunchedEffect(key1 = isChatImageClicked) {
        if (isChatImageClicked) {
            fullChatImageScale.animateTo(1f)
        } else {
            fullChatImageScale.animateTo(0f)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(chats.value.size) { index ->

                val item = chats.value[index]
                val isSelectedItem = selectedItemIndex.value == index
                val interactionSource = remember { MutableInteractionSource() }

                val lastMsgTime = item?.lastMessageTime

                val calender = Calendar.getInstance()
                val currentDate = calender.time

                if (!isSelected) {
                    selectedItemIndex.value = null
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color =
                            if (isSelected && isSelectedItem) {
                                if (isSystemInDarkTheme()) LowGreenDark else LowGreenLight
                            } else Color.Unspecified
                        )
                        .indication(interactionSource = interactionSource, LocalIndication.current)
                        .pointerInput(index) {
                            detectTapGestures(
                                onLongPress = {
                                    val newIsSelected = !isSelectedItem
                                    onChatLongPress.invoke(
                                        item?.chatId ?: "",
                                        newIsSelected,
                                        item?.username ?: "",
                                        item?.usernameGivenByChatCreator ?: "",
                                        item?.phoneNumber ?: ""
                                    )
                                    selectedItemIndex.value = if (newIsSelected) index else null
                                },
                                onPress = {
                                    val press = PressInteraction.Press(it)
                                    interactionSource.emit(press)
                                    tryAwaitRelease()
                                    interactionSource.emit(PressInteraction.Release(press))
                                },
                                onTap = {
                                    if (isSelected && isSelectedItem) {
                                        onTapIfSelected.invoke()
                                    } else {
                                        item?.chatId?.let { chatId ->
                                            navHostController.navigate(
                                                MainDest.SINGLE_CHAT_SCREEN(
                                                    chatId = chatId
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(all = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier
                                .width(40.dp)
                                .height(40.dp)
                                .clip(shape = RoundedCornerShape(30.dp))
                                .clickable {
                                    isChatImageDialogOpen = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.LightGray
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                if (item?.profilePic.isNullOrEmpty()) {
                                    Icon(
                                        modifier = Modifier
                                            .align(alignment = Alignment.Center),
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                } else {
                                    SubcomposeAsyncImage(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        model = item?.profilePic ?: "",
                                        contentDescription = "Chat Profile Pic",
                                        contentScale = ContentScale.FillWidth,
                                        loading = {
                                            Icon(
                                                modifier = Modifier
                                                    .width(20.dp)
                                                    .height(20.dp)
                                                    .align(alignment = Alignment.Center),
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Loading Chat Image",
                                                tint = Color.White,
                                            )
                                        },
                                        error = {
                                            Icon(
                                                modifier = Modifier
                                                    .width(20.dp)
                                                    .height(20.dp)
                                                    .align(alignment = Alignment.Center),
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Error Chat Image",
                                                tint = Color.White,
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier
                                        .weight(1f),
                                    text = if (item?.usernameGivenByChatCreator.isNullOrEmpty())
                                        item?.phoneNumber
                                            ?: "" else item?.usernameGivenByChatCreator ?: "",
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (lastMsgTime != null) {
                                        if (lastMsgTime.toDate().day == currentDate.day)
                                            formatTimestampToAmPm(lastMsgTime)
                                        else
                                            formatTimestampToDate(lastMsgTime)
                                    } else "",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = if (item?.unreadCount != 0 && item?.unreadCount != null) Green else Color.Unspecified
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {

                                LastMessage(chat = item) // Last Message

                                Spacer(modifier = Modifier.weight(1f))
                                if (item?.unreadCount != 0 && item?.unreadCount != null) {
                                    Card(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(20.dp)
                                            .clip(shape = RoundedCornerShape(20.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Green
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                modifier = Modifier,
                                                text = item?.unreadCount.toString(),
                                                style = TextStyle(
                                                    fontSize = 10.sp,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isChatImageDialogOpen) {
                    ChatImageDialog(
                        onDismiss = {
                            isChatImageDialogOpen = false
                        },
                        chat = item,
                        chatImageDialogScale = chatImageDialogScale.value,
                        navHostController = navHostController,
                        onImageClicked = {
                            isChatImageClicked = true
                            isChatImageDialogOpen = false
                        },
                    )
                }

                if (isChatImageClicked) {
                    FullScreenImageDialog(
                        imageString = item?.profilePic ?: "",
                        imageBitmap = null,
                        imageBy = if (item?.usernameGivenByChatCreator.isNullOrEmpty())
                            item?.phoneNumber
                                ?: "" else item?.usernameGivenByChatCreator
                            ?: "",
                        scale = fullChatImageScale.value,
                        onDismiss = {
                            isChatImageClicked = false
                        }
                    )
                }

            }
        }
        if (chats.value.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "No Chats Available")
            }
        }

    }
}

@Composable
fun ChatImageDialog(
    onDismiss: () -> Unit,
    chat: ChatModel?,
    chatImageDialogScale: Float,
    navHostController: NavHostController,
    onImageClicked: () -> Unit,
) {

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(16.dp)
                    .scale(chatImageDialogScale)
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = {
                                onImageClicked.invoke()
                            }
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        if (chat?.profilePic.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(160.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.LightGray
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp),
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Chat Image icon when its null or empty",
                                        tint = Color.White,
                                    )
                                }
                            }
                        } else {
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                model = chat?.profilePic ?: "",
                                contentDescription = "Chat Image",
                                contentScale = ContentScale.Fit,
                                loading = {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(shape = RoundedCornerShape(180.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.LightGray
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .height(50.dp),
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Loading Chat Image",
                                                tint = Color.White,
                                            )
                                        }
                                    }
                                },
                                error = {
                                    Icon(
                                        modifier = Modifier
                                            .align(alignment = Alignment.Center),
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error Chat Image",
                                        tint = Color.White,
                                    )
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(alignment = Alignment.TopCenter)
                                .background(color = Color.Black.copy(alpha = 0.3f))
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(all = 10.dp),
                                text = if (chat?.usernameGivenByChatCreator.isNullOrEmpty())
                                    chat?.phoneNumber
                                        ?: "" else chat?.usernameGivenByChatCreator ?: "",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally),
                        onClick = {
                            onDismiss.invoke()
                        navHostController.navigate(MainDest.SINGLE_CHAT_SCREEN(
                            chatId = chat?.chatId ?: ""
                        ))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Going to message with Chat Icon",
                            tint = Green
                        )
                    }

                }
            }
        }
    )
}