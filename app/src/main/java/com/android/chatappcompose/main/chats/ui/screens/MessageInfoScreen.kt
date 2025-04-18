@file:Suppress("SENSELESS_COMPARISON")

package com.android.chatappcompose.main.chats.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.android.chatappcompose.MainDest
import com.android.chatappcompose.core.domain.common_function.formatTimeStampWithMonthName
import com.android.chatappcompose.core.domain.common_function.formatTimestampToAmPm
import com.android.chatappcompose.core.domain.common_function.formatTimestampToDate
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.main.chats.domain.constants.FontSize
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import com.android.chatappcompose.main.chats.ui.composables.FullScreenMessageImageDialog
import com.android.chatappcompose.settings.ui.screens.chats.defaultWallpapersUrl.DefaultChatWallpapersUrl
import com.android.chatappcompose.ui.theme.Green
import com.android.chatappcompose.ui.theme.LowGreenDark
import com.android.chatappcompose.ui.theme.LowGreenLight
import com.android.chatappcompose.ui.theme.RepliedMessageContainerColor
import com.android.chatappcompose.ui.theme.SearchBarColorLight
import kotlinx.coroutines.launch

@Composable
fun MessageInfoScreen(
    navHostController: NavHostController,
    args: MainDest.MESSAGE_INFO_SCREEN,
    chatViewModel: ChatViewModel,
    snackbarHostState: SnackbarHostState
) {

    val scrollState = rememberScrollState()
    val boxScrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val messageById = chatViewModel.messageById.collectAsStateWithLifecycle()
    val chat = chatViewModel.chatById.collectAsStateWithLifecycle()
    val currentUser = chatViewModel.currentUser.collectAsStateWithLifecycle()
    val chatsSettings = chatViewModel.chatsSettings.collectAsStateWithLifecycle()

    var isMsgImageClicked = remember { mutableStateOf(false) }
    var fullScreenImageScale = remember { Animatable(0f) }


    LaunchedEffect(key1 = args.messageKey) {
        if (args?.chatId != null && args?.messageKey != null) {
            chatViewModel.getMessageById(args.chatId, args.messageKey)
            chatViewModel.getChatByChatId(args.chatId)
        }
    }

    LaunchedEffect(key1 = isMsgImageClicked.value) {
        if (isMsgImageClicked.value) {
            scope.launch {
                fullScreenImageScale.animateTo(1f)
            }
        } else {
            scope.launch {
                fullScreenImageScale.animateTo(0f)
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(state = scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(state = boxScrollState)
            ) {
                if (!chatsSettings.value?.chatWallpaper.isNullOrEmpty()) {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxSize(),
                        model = chatsSettings.value?.chatWallpaper ?: "",
                        contentDescription = "User Chat Wallpaper Image",
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxSize(),
                        model = if (isSystemInDarkTheme())
                            DefaultChatWallpapersUrl.CHAT_WALLPAPER_DARK_URL
                        else DefaultChatWallpapersUrl.CHAT_WALLPAPER_LIGHT_URL,
                        contentDescription = "User Chat Wallpaper Image",
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.Black.copy(0.2f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(all = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    navHostController.navigateUp()
                                }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back Navigation Icon",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Message Info",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(all = 10.dp)
                                .padding(start = if (!messageById.value?.imageUrl.isNullOrEmpty()) 70.dp else 30.dp)
                                .padding(top = 10.dp)
                                .align(alignment = Alignment.TopEnd),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystemInDarkTheme()) LowGreenDark else Green,
                                contentColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(all = 5.dp)
                            ) {
                                // replied Message Container
                                if (!messageById.value?.repliedMessageKey.isNullOrEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .widthIn(min = 50.dp)
                                            .padding(bottom = if (messageById.value?.imageUrl.isNullOrEmpty()) 0.dp else 5.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = RepliedMessageContainerColor,
                                            contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(all = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(end = 10.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                ) {
                                                    Text(
                                                        text = messageById.value?.repliedMessageStatus
                                                            ?: "",
                                                        style = TextStyle(
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = if (messageById.value?.repliedMessageStatus == "You") LowGreenLight else Color.Black
                                                    )
                                                    if (messageById.value?.message.isNullOrEmpty()) {
                                                        if (!messageById.value?.imageUrl.isNullOrEmpty()) {
                                                            Text(
                                                                text = "Photo",
                                                                style = TextStyle(
                                                                    fontSize = 12.sp
                                                                )
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = messageById.value?.repliedMessage
                                                                ?: "",
                                                            style = TextStyle(
                                                                fontSize = 12.sp
                                                            )
                                                        )
                                                    }
                                                }
                                                if (!messageById.value?.imageUrl.isNullOrEmpty()) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                                if (!messageById.value?.repliedMessageImageUrl.isNullOrEmpty()) {
                                                    AsyncImage(
                                                        modifier = Modifier
                                                            .width(35.dp)
                                                            .height(35.dp)
                                                            .clip(
                                                                shape = RoundedCornerShape(
                                                                    10.dp
                                                                )
                                                            ),
                                                        model = messageById.value?.repliedMessageImageUrl
                                                            ?: "",
                                                        contentDescription = null,
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!messageById.value?.imageUrl.isNullOrEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(
                                                max = 300.dp
                                            )
                                            .pointerInput(true) {
                                                detectTapGestures(
                                                    onTap = {
                                                        isMsgImageClicked.value = true
                                                    }
                                                )
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        if (!isMsgImageClicked.value) {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                model = messageById.value?.imageUrl ?: "",
                                                contentDescription = "Sender Image Message",
                                                contentScale = ContentScale.Crop,
                                            )
                                        }
                                    }
                                }
                                Text(
                                    modifier = Modifier
                                        .padding(
                                            start = if (
                                                !messageById.value?.imageUrl.isNullOrEmpty() ||
                                                !messageById.value?.repliedMessageKey.isNullOrEmpty()
                                            ) 5.dp else 0.dp,

                                            top = if (
                                                !messageById.value?.imageUrl.isNullOrEmpty() ||
                                                !messageById.value?.repliedMessageKey.isNullOrEmpty()
                                            ) 5.dp else 0.dp
                                        ),
                                    text = messageById.value?.message ?: "",
                                    style = TextStyle(
                                        fontSize = if (
                                            !messageById.value?.message.isNullOrEmpty()
                                        ) {
                                            if (chatsSettings.value?.fontSize == "Small")
                                                FontSize.SMALL.sp
                                            else if (chatsSettings.value?.fontSize == "Large")
                                                FontSize.LARGE.sp
                                            else FontSize.MEDIUM.sp
                                        } else 0.sp,
                                        color = Color.White,
                                    )
                                )

                                Text(
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .align(alignment = Alignment.End),
                                    text = messageById.value?.timeStamp?.let {
                                        formatTimestampToAmPm(it)
                                    } ?: "",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight,
                contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp)
            ) {
                Text(
                    text = "Delivered Time",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = formatTimestampToAmPm(messageById.value?.timeStamp),
                    style = TextStyle(
                        fontSize = 14.sp
                    )
                )

            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight,
                contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp)
            ) {
                Text(
                    text = "Delivered Date",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "${
                        formatTimeStampWithMonthName(
                            messageById.value?.timeStamp,
                            messageById.value?.monthName.toString()
                        )
                    },   ${formatTimestampToDate(messageById.value?.timeStamp)}",
                    style = TextStyle(
                        fontSize = 14.sp
                    )
                )

            }
        }

        if (isMsgImageClicked.value) {
            FullScreenMessageImageDialog(
                item = messageById.value,
                messageBy = "You",
                chatViewModel = chatViewModel,
                chatId = args.chatId,
                chat = chat.value,
                currentUser = currentUser.value,
                scale = fullScreenImageScale.value,
                scope = scope,
                snackbarHostState = snackbarHostState,
                onDismiss = {
                    isMsgImageClicked.value = false
                }
            )
        }

        if (args.chatId == null || args.messageKey == null) {
            LoadingDialog()
        }
    }
}