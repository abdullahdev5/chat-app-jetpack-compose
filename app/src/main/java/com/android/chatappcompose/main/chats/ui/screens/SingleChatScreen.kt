@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class, FlowPreview::class
)

package com.android.chatappcompose.main.chats.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.android.chatappcompose.MainDest
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.common_function.formatTimeStampWithMonthName
import com.android.chatappcompose.core.domain.common_function.formatTimestampToAmPm
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.core.ui.commomComposables.CopyTextToClipBoard
import com.android.chatappcompose.core.ui.commomComposables.LastMessage
import com.android.chatappcompose.core.ui.commomComposables.CircularProgressIndicatorWithValue
import com.android.chatappcompose.core.ui.commomComposables.FullScreenImageDialog
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.PickImageFromCameraAndGalleryDialog
import com.android.chatappcompose.main.chats.domain.constants.FontSize
import com.android.chatappcompose.main.chats.domain.constants.MessageReactionMenuList
import com.android.chatappcompose.main.chats.domain.model.ChatModel
import com.android.chatappcompose.main.chats.domain.model.MessageModel
import com.android.chatappcompose.main.chats.domain.model.MessageReactionModel
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import com.android.chatappcompose.main.chats.ui.composables.FullScreenMessageImageDialog
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.android.chatappcompose.settings.ui.screens.chats.defaultWallpapersUrl.DefaultChatWallpapersUrl
import com.android.chatappcompose.ui.theme.Green
import com.android.chatappcompose.ui.theme.LowGreenDark
import com.android.chatappcompose.ui.theme.LowGreenLight
import com.android.chatappcompose.ui.theme.RepliedMessageContainerColor
import com.android.chatappcompose.ui.theme.SearchBarColorLight
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("StateFlowValueCalledInComposition", "CoroutineCreationDuringComposition")
@Composable
fun SingleChatScreen(
    navHostController: NavHostController,
    chatViewModel: ChatViewModel,
    args: MainDest.SINGLE_CHAT_SCREEN,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val reactionMsgSheetState = rememberModalBottomSheetState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val chat by chatViewModel.chatById.collectAsStateWithLifecycle()
    val messagesList by chatViewModel.messagesList.collectAsStateWithLifecycle()
    val currentUser by chatViewModel.currentUser.collectAsStateWithLifecycle()
    val chatsSettings by chatViewModel.chatsSettings.collectAsStateWithLifecycle()

    var message by rememberSaveable { mutableStateOf("") }
    var isMessageSended by remember { mutableStateOf(true) }
    var isImageMessageSended by remember { mutableStateOf(true) }
    var isCameraAndGalleryPickDialogOpen by remember { mutableStateOf(false) }
    var isDeleteMsgDialogOpen by remember { mutableStateOf(false) }
    var isMsgsDeleted by remember { mutableStateOf(true) } // true By Default
    var progress by remember { mutableStateOf(0f) }
    var isMoreAboutSelectedMsgMenuVisible by remember { mutableStateOf(false) }
    var isImageFromCameraOrGalleryClicked by remember { mutableStateOf(false) }
    var fullScreenImageFromCameraOrGalleyScale = remember { Animatable(0f) }

    var topMessageDate by remember { mutableStateOf<String?>(null) }
    val lazyListState = rememberLazyListState()

    val messageById by chatViewModel.messageById.collectAsStateWithLifecycle()

    val repliedMessageByIdWhenDragComplete by chatViewModel.repliedMessageByIdWhenDragComplete.collectAsStateWithLifecycle()

    // Camera Related
    val cameraImageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = {
            cameraImageBitmap.value = it
        }
    )
    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isPermissionGranted ->
            if (isPermissionGranted) {
                cameraLauncher.launch() // this is the camera launcher when user allow the permission then it's launch the camera.
            }
        }
    )

    // Gallery Related
    val context = LocalContext.current
    val galleryImageUri = remember { mutableStateOf<Uri?>(null) }
    val galleryImageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    // Gallery Launcher
    val galleyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        galleryImageUri.value = it
    }



    LaunchedEffect(args.chatId) {
        chatViewModel.getChatByChatId(chatId = args.chatId)
        chatViewModel.getMessages(chatId = args.chatId)
        chatViewModel.updateChatUserFieldsInCurrentUser(chatId = args.chatId)
    }
    LaunchedEffect(key1 = true) {
        chatViewModel.updateUnreadCount(
            chatId = args.chatId
        )
    }

    LaunchedEffect(key1 = lazyListState.firstVisibleItemIndex) {
        val visibleItemIndex = lazyListState.firstVisibleItemIndex + 1 // Account for reverse layout
        if (visibleItemIndex < messagesList.size) {
            topMessageDate = formatTimeStampWithMonthName(
                messagesList[visibleItemIndex]?.timeStamp,
                messagesList[visibleItemIndex]?.monthName.toString()
            )
        } else {
            topMessageDate = null
        }
    }

    LaunchedEffect(key1 = galleryImageUri.value) {
        galleryImageUri.value?.let {
            galleryImageBitmap.value = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    it
                )
            } else {
                val source =
                    ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    LaunchedEffect(
        key1 = galleryImageBitmap.value,
        cameraImageBitmap.value,
        chatViewModel.repliedMsgItemId.value
    ) {
        if (galleryImageBitmap.value != null ||
            cameraImageBitmap.value != null ||
            chatViewModel.repliedMsgItemId.value != null
        ) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(key1 = messagesList) {
        if (messagesList.isNotEmpty()) {
            lazyListState.scrollToItem(messagesList.size - 1)
        }
    }

    LaunchedEffect(key1 = chatViewModel.repliedMsgItemId.value) {
        chatViewModel.repliedMsgItemId.value?.let {
            chatViewModel.getRepliedMsgByIdWhenDragComplete(
                chatId = args.chatId,
                messageKey = it
            )
        }
    }

    LaunchedEffect(key1 = isImageFromCameraOrGalleryClicked) {
        if (isImageFromCameraOrGalleryClicked) {
            scope.launch {
                fullScreenImageFromCameraOrGalleyScale.animateTo(1f)
            }
        } else {
            scope.launch {
                fullScreenImageFromCameraOrGalleyScale.animateTo(0f)
            }
        }
    }

    BackHandler {
        if (galleryImageUri.value != null || cameraImageBitmap.value != null) {

            galleryImageUri.value = null
            cameraImageBitmap.value = null

        } else if (chatViewModel.isMessageSelected.value) {

            chatViewModel.isMessageSelected.value = false
            chatViewModel.selectedSenderItemsIds.clear()

        } else {
            chatViewModel.depopulateMessages()
            navHostController.navigateUp()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            if (!chatViewModel.isMessageSelected.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .pointerInput(key1 = true) {
                                detectTapGestures(
                                    onTap = {
                                        if (galleryImageUri.value != null || cameraImageBitmap.value != null) {

                                            galleryImageUri.value = null
                                            cameraImageBitmap.value = null

                                        } else {
                                            chatViewModel.depopulateMessages()
                                            navHostController.navigateUp()
                                        }
                                    }
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Navigation Icon"
                        )
                        Card(
                            modifier = Modifier
                                .width(35.dp)
                                .height(35.dp)
                                .clip(shape = RoundedCornerShape(30.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.LightGray
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                if (chat?.profilePic.isNullOrEmpty()) {
                                    Icon(
                                        modifier = Modifier
                                            .align(alignment = Alignment.Center),
                                        imageVector = Icons.Default.Person,
                                        tint = Color.White,
                                        contentDescription = null
                                    )
                                } else {
                                    SubcomposeAsyncImage(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        model = ImageRequest.Builder(context = LocalContext.current)
                                            .data(chat?.profilePic ?: "")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Chat Image",
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
                    }
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(end = 20.dp),
                            text = if (chat?.usernameGivenByChatCreator.isNullOrEmpty()) chat?.phoneNumber
                                ?: "" else chat?.usernameGivenByChatCreator!!,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        LastMessage(chat = chat)
                    }
                }
            } else {
                val allSelectedItemsSize =
                    chatViewModel.selectedSenderItemsIds.size + chatViewModel.selectedReceiverItemsIds.size
                TopAppBar(
                    modifier = Modifier
                        .statusBarsPadding(),
                    title = {
                        Text(text = allSelectedItemsSize.toString())
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                chatViewModel.isMessageSelected.value = false
                                chatViewModel.selectedSenderItemsIds.clear()
                                chatViewModel.selectedReceiverItemsIds.clear()
                            }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        if (allSelectedItemsSize == 1) {
                            IconButton(onClick = {
                                messageById?.let {
                                    chatViewModel.selectedSenderItemsIds.clear()
                                    chatViewModel.selectedReceiverItemsIds.clear()
                                    chatViewModel.isMessageSelected.value = false

                                    chatViewModel.repliedMsgItemId.value = it.key
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Reply,
                                    contentDescription = "replied message icon"
                                )
                            }
                        }

                        IconButton(onClick = {
                            if (chatViewModel.selectedReceiverItemsIds.size != 0) {

                                val allSelectedIds =
                                    chatViewModel.selectedSenderItemsIds + chatViewModel.selectedReceiverItemsIds

                                scope.launch(Dispatchers.Main) {
                                    chatViewModel.deleteMsgForSender(
                                        args.chatId,
                                        allSelectedIds as List<String>
                                    ).collect { result ->
                                        when (result) {
                                            is ResultState.Failure -> {
                                                isMsgsDeleted = true
                                                snackbarHostState.showSnackbar(
                                                    message = result.error.message.toString(),
                                                    duration = SnackbarDuration.Short
                                                )
                                            }

                                            ResultState.Loading -> {
                                                isMsgsDeleted = false
                                            }

                                            is ResultState.Success -> {
                                                chatViewModel.updateLastMessageOfChat(
                                                    chatId = args.chatId,
                                                    senderLastMsg = "You Deleted This Message",
                                                    receiverLastMsg = null,
                                                    lastMsgImageUrl = null,
                                                    timestamp = null
                                                )
                                                isMsgsDeleted = true
                                                chatViewModel.isMessageSelected.value = false
                                                chatViewModel.selectedSenderItemsIds.clear()
                                                chatViewModel.selectedReceiverItemsIds.clear()
                                            }

                                            is ResultState.Progress -> TODO()
                                        }
                                    }
                                }
                            } else {
                                isDeleteMsgDialogOpen = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Message Icon"
                            )
                        }


                        if (allSelectedItemsSize == 1) {

                            IconButton(onClick = {
                                isMoreAboutSelectedMsgMenuVisible =
                                    !isMoreAboutSelectedMsgMenuVisible
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More About selected message"
                                )
                            }

                            if (isMoreAboutSelectedMsgMenuVisible) {
                                MoreAboutSelectedMessageDropDownMenu(
                                    isMoreAboutSelectedMsgMenuVisible = isMoreAboutSelectedMsgMenuVisible,
                                    selectedReceiverMsgSize = chatViewModel.selectedReceiverItemsIds.size,
                                    onInfoClicked = {
                                        navHostController.navigate(
                                            MainDest.MESSAGE_INFO_SCREEN(
                                                chatId = args.chatId,
                                                messageKey = chatViewModel.selectedSenderItemsIds.firstOrNull()
                                                    ?: ""
                                            )
                                        )
                                    },
                                    onCopyClicked = {
                                        if (!messageById?.message.isNullOrEmpty()) {
                                            CopyTextToClipBoard(
                                                messageById?.message ?: "",
                                                context,
                                                onCopy = {
                                                    Toast.makeText(
                                                        context,
                                                        "Message Copied",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "No Message To Copy",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    onDismiss = {
                                        isMoreAboutSelectedMsgMenuVisible = false
                                    },
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            SingleChatScreenBottomBar(
                message = message,
                onMessageChange = { message = it },
                onMessageEmpty = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "message can't be empty",
                            duration = SnackbarDuration.Short,
                        )
                    }
                },
                chatId = args.chatId,
                chatViewModel = chatViewModel,
                currentUser = currentUser,
                galleryImageUri = galleryImageUri.value,
                galleryImageBitmap = galleryImageBitmap.value,
                cameraImageBitmap = cameraImageBitmap.value,
                onCameraTrailingIconClicked = { isCameraAndGalleryPickDialogOpen = true },
                onImageCancel = {
                    galleryImageUri.value = null
                    galleryImageBitmap.value = null
                    cameraImageBitmap.value = null
                },
                chat = chat,
                replyMsgItem = repliedMessageByIdWhenDragComplete,
                onReplyMsgContainerClicked = {
                    scope.launch {
                        repliedMessageByIdWhenDragComplete?.let { repliedMsg ->
                            val index =
                                messagesList.indexOfFirst { index ->
                                    index?.key == repliedMsg.key
                                }
                            index?.let {
                                lazyListState.animateScrollToItem(
                                    it
                                )
                            } ?: 0
                            chatViewModel.clickedRepliedMsgKey.value =
                                repliedMessageByIdWhenDragComplete?.key
                                    ?: ""
                            delay(1000L)
                            chatViewModel.clickedRepliedMsgKey.value =
                                ""
                        }
                    }
                },
                focusRequester = focusRequester,
                onTextMessageSuccess = {
                    isMessageSended = true
                    message = ""
                    scope.launch {
                        lazyListState.scrollToItem(messagesList.size - 1)
                    }
                    chatViewModel.repliedMsgItemId.value?.let {
                        chatViewModel.repliedMsgItemId.value = null
                    }
                },
                onTextMessageLoading = {
                    isMessageSended = false
                },
                onTextMessageFailure = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = it,
                            duration = SnackbarDuration.Short,
                        )
                        isMessageSended = false
                        message = ""
                        lazyListState.scrollToItem(0, 0)
                    }
                },
                onImageUploadSuccess = {
                    galleryImageUri.value = null
                    cameraImageBitmap.value = null
                    chatViewModel.repliedMsgItemId.value?.let {
                        chatViewModel.repliedMsgItemId.value = null
                    }
                },
                onImageUploadLoading = {
                    isImageMessageSended = false
                },
                onImageUploadFailure = {
                    isImageMessageSended = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = it,
                            duration = SnackbarDuration.Short,
                        )
                        chatViewModel.cancelProgressNotification()
                        chatViewModel.notifySimpleNotificationForMessage(
                            context = context,
                            chatId = args.chatId,
                            title = "Failed to Send Image!",
                            text = ""
                        )
                        galleryImageUri.value = null
                        cameraImageBitmap.value = null
                        lazyListState.scrollToItem(messagesList.size - 1)
                    }
                },
                onImageUploadProgress = {
                    if (it != progress) {
                        progress = it
                        chatViewModel.onImageMessageSendNotificationWithProgress(
                            context = context,
                            progress = progress,
                        )
                    }
                },
                onImageMessageStores = { key ->
                    chatViewModel.updateMessageSended(
                        chatId = args.chatId,
                        messageKey = key,
                        sended = true
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Photo Sent Successfully"
                        )
                        lazyListState.scrollToItem(messagesList.size - 1)
                    }
                    chatViewModel.cancelProgressNotification()
                    chatViewModel.notifySimpleNotificationForMessage(
                        context = context,
                        chatId = args.chatId,
                        title = "Image Sent Successfully",
                        text = ""
                    )
                    isImageMessageSended = true
                    progress = 1f
                    message = ""
                },
                isImageMessageSended = isImageMessageSended,
                scope = scope,
                isLandscape = isLandscape,
                onSendIconClickWhenImageNotEmpty = {

                },
                onImageFromCameraOrGalleryClicked = {
                    isImageFromCameraOrGalleryClicked = true
                }
            )

//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .navigationBarsPadding()
//            ) {
//                AnimatedVisibility(
//                    visible = galleryImageUri.value != null ||
//                            cameraImageBitmap.value != null ||
//                            chatViewModel.repliedMsgItemId.value != null
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(10.dp)
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .weight(1f)
//                        ) {
//                            if (chatViewModel.repliedMsgItemId.value != null) {
//                                // Replied Msg Container
//                                Column(
//                                    modifier = Modifier
//                                        .heightIn(min = 30.dp, max = 300.dp)
//                                        .clip(
//                                            shape = RoundedCornerShape(
//                                                topStart = 25.dp,
//                                                topEnd = 25.dp
//                                            )
//                                        )
//                                        .background(color = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Card(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .clip(shape = RoundedCornerShape(30.dp))
//                                            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
//                                            .pointerInput(true) {
//                                                detectTapGestures(
//                                                    onTap = {
//                                                        scope.launch {
//                                                            repliedMessageByIdWhenDragComplete?.let { repliedMsg ->
//                                                                val index =
//                                                                    messagesList.indexOfFirst { index ->
//                                                                        index?.key == repliedMsg.key
//                                                                    }
//                                                                index?.let {
//                                                                    lazyListState.animateScrollToItem(
//                                                                        it
//                                                                    )
//                                                                } ?: 0
//                                                                chatViewModel.clickedRepliedMsgKey.value =
//                                                                    repliedMessageByIdWhenDragComplete?.key
//                                                                        ?: ""
//                                                                delay(1000L)
//                                                                chatViewModel.clickedRepliedMsgKey.value =
//                                                                    ""
//                                                            }
//                                                        }
//                                                    }
//                                                )
//                                            },
//                                        colors = CardDefaults.cardColors(
//                                            containerColor = if (isSystemInDarkTheme()) LowGreenDark else Color.LightGray,
//                                            contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
//                                        )
//                                    ) {
//                                        Box(
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .padding(all = 10.dp)
//                                        ) {
//                                            IconButton(
//                                                modifier = Modifier
//                                                    .width(15.dp)
//                                                    .height(15.dp)
//                                                    .align(alignment = Alignment.TopEnd),
//                                                onClick = {
//                                                    chatViewModel.repliedMsgItemId.value = null
//                                                }) {
//                                                Icon(
//                                                    imageVector = Icons.Default.Cancel,
//                                                    contentDescription = "Cancel Image Icon"
//                                                )
//                                            }
//                                            Row(
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .padding(end = 20.dp)
//                                            ) {
//                                                Column(
//                                                    modifier = Modifier
//                                                ) {
//                                                    if (repliedMessageByIdWhenDragComplete?.senderId == currentUser?.key) {
//                                                        Text(
//                                                            text = "You",
//                                                            style = TextStyle(
//                                                                fontSize = 15.sp,
//                                                                fontWeight = FontWeight.Bold
//                                                            ),
//                                                            color = LowGreenLight
//                                                        )
//                                                    } else {
//                                                        Text(
//                                                            text = if (chat?.usernameGivenByChatCreator.toString()
//                                                                    .isEmpty()
//                                                            )
//                                                                chat?.phoneNumber
//                                                                    ?: "" else chat?.usernameGivenByChatCreator
//                                                                ?: "",
//                                                            style = TextStyle(
//                                                                fontSize = 15.sp,
//                                                                fontWeight = FontWeight.Bold
//                                                            ),
//                                                            color = Color.Black
//                                                        )
//                                                    }
//                                                    if (repliedMessageByIdWhenDragComplete?.message.isNullOrEmpty()) {
//                                                        if (!repliedMessageByIdWhenDragComplete?.imageUrl.isNullOrEmpty()) {
//                                                            Text(
//                                                                text = "Photo",
//                                                                style = TextStyle(
//                                                                    fontSize = 12.sp
//                                                                )
//                                                            )
//                                                        }
//                                                    } else {
//                                                        Text(
//                                                            text = repliedMessageByIdWhenDragComplete?.message
//                                                                ?: "",
//                                                            style = TextStyle(
//                                                                fontSize = 12.sp
//                                                            )
//                                                        )
//                                                    }
//                                                }
//                                                Spacer(modifier = Modifier.weight(1f))
//                                                if (!repliedMessageByIdWhenDragComplete?.imageUrl.isNullOrEmpty()) {
//                                                    AsyncImage(
//                                                        modifier = Modifier
//                                                            .width(40.dp)
//                                                            .height(40.dp)
//                                                            .clip(shape = RoundedCornerShape(10.dp)),
//                                                        model = repliedMessageByIdWhenDragComplete?.imageUrl
//                                                            ?: "",
//                                                        contentDescription = null,
//                                                        contentScale = ContentScale.FillWidth
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            if (galleryImageUri.value != null || cameraImageBitmap.value != null) {
//                                Column(
//                                    modifier = Modifier
//                                        .height(height = if (isLandscape) 100.dp else 200.dp)
//                                        .clip(
//                                            shape = RoundedCornerShape(
//                                                topStart = if (chatViewModel.repliedMsgItemId.value == null)
//                                                    25.dp else 0.dp,
//                                                topEnd = if (chatViewModel.repliedMsgItemId.value == null)
//                                                    25.dp else 0.dp,
//                                                bottomStart = 0.dp,
//                                                bottomEnd = 0.dp
//                                            )
//                                        )
//                                        .background(color = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight),
//                                ) {
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                    ) {
//                                        // Image
//                                        AsyncImage(
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .padding(start = 5.dp, end = 5.dp, top = 5.dp)
//                                                .clip(shape = RoundedCornerShape(15.dp)),
//                                            model = galleryImageBitmap.value?.let {
//                                                    it
//                                                } ?: cameraImageBitmap.value,
//                                            contentDescription = "Gallery Image",
//                                            contentScale = ContentScale.FillWidth
//                                        )
//
//                                        IconButton(
//                                            modifier = Modifier
//                                                .width(25.dp)
//                                                .height(25.dp)
//                                                .padding(end = 10.dp, top = 10.dp)
//                                                .align(alignment = Alignment.TopEnd),
//                                            onClick = {
//                                                galleryImageUri.value = null
//                                                cameraImageBitmap.value = null
//                                            }) {
//                                            Icon(
//                                                imageVector = Icons.Default.Cancel,
//                                                contentDescription = "Cancel Image Icon",
//                                                tint = Color.White
//                                            )
//                                        }
//                                    }
//                                }
//
//                            }
//                        }
//                        IconButton(
//                            modifier = Modifier
//                                .clip(shape = RoundedCornerShape(25.dp)),
//                            colors = IconButtonDefaults.iconButtonColors(
//                                containerColor = Color.Transparent,
//                                contentColor = Color.Transparent,
//                            ),
//                            onClick = {},
//                            enabled = false
//                        ) {
//
//                        }
//                    }
//                }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    OutlinedTextField(
//                        modifier = Modifier
//                            .weight(1f)
//                            .heightIn(
//                                min = 30.dp,
//                                max = 150.dp
//                            )
//                            .clip(
//                                shape = RoundedCornerShape(
//                                    bottomStart = 25.dp, bottomEnd = 25.dp,
//                                    topStart = if (
//                                        galleryImageUri.value != null || cameraImageBitmap.value != null
//                                        || chatViewModel.repliedMsgItemId.value != null
//                                    ) 0.dp else 25.dp,
//                                    topEnd = if (
//                                        galleryImageUri.value != null || cameraImageBitmap.value != null
//                                        || chatViewModel.repliedMsgItemId.value != null
//                                    ) 0.dp else 25.dp
//                                )
//                            )
//                            .focusRequester(focusRequester),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            unfocusedBorderColor = Color.Transparent,
//                            focusedBorderColor = Color.Transparent,
//                            focusedContainerColor = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight,
//                            unfocusedContainerColor = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight,
//                        ),
//                        value = message,
//                        onValueChange = {
//                            message = it
//                        },
//                        placeholder = {
//                            Text(
//                                text = if (
//                                    galleryImageUri.value != null || cameraImageBitmap.value != null
//                                ) "Caption (optional)" else "Message"
//                            )
//                        },
//                        singleLine = false,
//                        keyboardOptions = KeyboardOptions.Default,
//                        trailingIcon = {
//                            IconButton(onClick = {
//                                isCameraAndGalleryPickDialogOpen = true
//                            }) {
//                                Icon(
//                                    imageVector = Icons.Default.PhotoCamera,
//                                    contentDescription = "Camera Icon"
//                                )
//                            }
//                        },
//                    )
//                    IconButton(
//                        modifier = Modifier
//                            .clip(shape = RoundedCornerShape(25.dp))
//                            .background(color = Green)
//                            .align(alignment = Alignment.Bottom),
//                        enabled = if (isImageMessageSended) true else false,
//                        onClick = {
//                            if (galleryImageUri.value != null || cameraImageBitmap.value != null) {
//                                scope.launch(Dispatchers.Main) {
//                                    chatViewModel.onImageMessageUpload(
//                                        imageBitmap = galleryImageBitmap.value?.let {
//                                            galleryImageBitmap.value
//                                        } ?: cameraImageBitmap.value!!,
//
//                                        ).collect { result ->
//                                        when (result) {
//                                            is ResultState.Failure -> {
//                                                isImageMessageSended = false
//                                                snackbarHostState.showSnackbar(
//                                                    message = result.error.toString(),
//                                                    duration = SnackbarDuration.Short,
//                                                )
//                                                galleryImageUri.value = null
//                                                cameraImageBitmap.value = null
//                                                lazyListState.scrollToItem(messagesList.size - 1)
//                                            }
//
//                                            ResultState.Loading -> {
//                                                isImageMessageSended = false
//                                            }
//
//                                            is ResultState.Progress -> {
//                                                progress = result.progress
//                                            }
//
//                                            is ResultState.Success -> {
//
//                                                if (message.toString().isEmpty()) {
//                                                    chatViewModel.updateLastMessageOfChat(
//                                                        chatId = args.chatId,
//                                                        senderLastMsg = "You: Photo",
//                                                        receiverLastMsg = "Photo",
//                                                        lastMsgImageUrl = result.data.toString(),
//                                                        timestamp = Timestamp.now()
//                                                    )
//                                                } else {
//                                                    chatViewModel.updateLastMessageOfChat(
//                                                        chatId = args.chatId,
//                                                        senderLastMsg = "You: $message",
//                                                        receiverLastMsg = message,
//                                                        lastMsgImageUrl = result.data.toString(),
//                                                        timestamp = Timestamp.now()
//                                                    )
//                                                }
//
//                                                chatViewModel.onImageMessageSent(
//                                                    result.data,
//                                                    message,
//                                                    args.chatId,
//                                                    repliedMessage = repliedMessageByIdWhenDragComplete?.message?.let {
//                                                        it
//                                                    } ?: "",
//                                                    repliedMessageImageUrl = repliedMessageByIdWhenDragComplete?.imageUrl?.let {
//                                                        it
//                                                    } ?: "",
//                                                    repliedMessageKey = chatViewModel.repliedMsgItemId.value?.let {
//                                                        it
//                                                    } ?: "",
//                                                    senderRepliedMessageStatus = if (
//                                                        repliedMessageByIdWhenDragComplete?.senderId == currentUser?.key
//                                                    ) "You" else chat?.phoneNumber,
//                                                    receiverRepliedMessageStatus = if (
//                                                        repliedMessageByIdWhenDragComplete?.senderId == currentUser?.key
//                                                    ) currentUser?.phoneNumber else "You",
//                                                    onImageMessageStores = { key ->
//                                                        isImageMessageSended = true
//                                                        progress = 1f
//                                                        chatViewModel.updateMessageSended(
//                                                            chatId = args.chatId,
//                                                            messageKey = key,
//                                                            sended = true
//                                                        )
//                                                        scope.launch {
//                                                            snackbarHostState.showSnackbar(
//                                                                message = "Photo Sended"
//                                                            )
//                                                            lazyListState.scrollToItem(messagesList.size - 1)
//                                                        }
//                                                    }
//                                                )
//                                                galleryImageUri.value = null
//                                                cameraImageBitmap.value = null
//                                                message = ""
//                                                chatViewModel.repliedMsgItemId.value?.let {
//                                                    chatViewModel.repliedMsgItemId.value = null
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                if (message.isEmpty()) {
//                                    scope.launch {
//                                        delay(1000L)
//                                        snackbarHostState.showSnackbar(
//                                            message = "message can't be empty",
//                                            duration = SnackbarDuration.Short
//                                        )
//                                    }
//                                } else {
//                                    scope.launch(Dispatchers.Main) {
//                                        chatViewModel.onTextMessageSent(
//                                            message = message,
//                                            chatId = chat?.chatId!!,
//                                            repliedMessage = repliedMessageByIdWhenDragComplete?.message?.let {
//                                                it
//                                            } ?: "",
//                                            repliedMessageImageUrl = repliedMessageByIdWhenDragComplete?.imageUrl?.let {
//                                                it
//                                            } ?: "",
//                                            repliedMessageKey = chatViewModel.repliedMsgItemId.value?.let {
//                                                it
//                                            } ?: "",
//                                            senderRepliedMessageStatus = if (
//                                                repliedMessageByIdWhenDragComplete?.senderId == currentUser?.key
//                                            ) "You" else chat?.phoneNumber,
//                                            receiverRepliedMessageStatus = if (
//                                                repliedMessageByIdWhenDragComplete?.senderId == currentUser?.key
//                                            ) currentUser?.phoneNumber else "You",
//                                        ).collect {
//                                            when (it) {
//                                                is ResultState.Failure -> {
//                                                    snackbarHostState.showSnackbar(
//                                                        message = it.error.toString(),
//                                                        duration = SnackbarDuration.Short,
//                                                    )
//                                                    isMessageSended = false
//                                                    message = ""
//                                                    lazyListState.scrollToItem(0, 0)
//                                                }
//
//                                                ResultState.Loading -> {
//                                                    isMessageSended = false
//                                                    // message = ""
//                                                }
//
//                                                is ResultState.Success -> {
//                                                    chatViewModel.updateLastMessageOfChat(
//                                                        chatId = args.chatId,
//                                                        senderLastMsg = "You: $message",
//                                                        receiverLastMsg = message,
//                                                        lastMsgImageUrl = null,
//                                                        Timestamp.now()
//                                                    )
//                                                    chatViewModel.updateMessageSended(
//                                                        chatId = args.chatId,
//                                                        messageKey = it.data.key,
//                                                        sended = true
//                                                    )
//                                                    isMessageSended = true
//                                                    message = ""
//                                                    lazyListState.scrollToItem(0, 0)
//                                                    chatViewModel.repliedMsgItemId.value = null
//                                                }
//
//                                                is ResultState.Progress -> TODO()
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                        }) {
//                        Icon(
//                            modifier = Modifier
//                                .padding(all = 10.dp),
//                            imageVector = Icons.Default.Send,
//                            tint = Color.White,
//                            contentDescription = "Send Message Icon"
//                        )
//                    }
//                }
//            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = lazyListState.canScrollForward) {
                Card(
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .clip(shape = RoundedCornerShape(25.dp))
                        .clickable {
                            scope.launch {
                                lazyListState.scrollToItem(messagesList.size - 1)
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color.DarkGray.copy(0.8f) else SearchBarColorLight.copy(
                            0.8f
                        ),
                        contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(20.dp)
                                .height(20.dp),
                            imageVector = Icons.Default.KeyboardDoubleArrowDown,
                            contentDescription = "Scroll to 0 index Icon",
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Chat Wallpaper
            if (!chatsSettings?.chatWallpaper.isNullOrEmpty()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                        .padding(top = 10.dp)
                        .clip(
                            shape = RoundedCornerShape(
                                topStart = 10.dp,
                                topEnd = 10.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        ),
                    model = chatsSettings?.chatWallpaper ?: "",
                    contentDescription = "User Chat Wallpaper Image",
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                        .padding(top = 10.dp)
                        .clip(
                            shape = RoundedCornerShape(
                                topStart = 10.dp,
                                topEnd = 10.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        ),
                    model = if (isSystemInDarkTheme())
                        DefaultChatWallpapersUrl.CHAT_WALLPAPER_DARK_URL
                    else DefaultChatWallpapersUrl.CHAT_WALLPAPER_LIGHT_URL,
                    contentDescription = "User Chat Wallpaper Image",
                    contentScale = ContentScale.Crop
                )
            }

            LazyColumn(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(paddingValues = paddingValues),
                state = lazyListState,
            ) {
                items(
                    count = messagesList.size,
                    key = { messagesList[it]?.key ?: "" }
                ) { index ->
                    val item = messagesList[index]

                    val senderCurrentUser = item?.senderId == currentUser?.key
                    val timestamp = item?.timeStamp


                    if (senderCurrentUser) {
                        SenderMessage(
                            item = item,
                            index = index,
                            timestamp = timestamp,
                            chatViewModel = chatViewModel,
                            messagesList = messagesList,
                            chatId = args.chatId,
                            context = context,
                            scope = scope,
                            reactionMsgSheetState = reactionMsgSheetState,
                            chat = chat,
                            currentUser = currentUser,
                            chatsSettings = chatsSettings,
                            lazyListState = lazyListState,
                            snackbarHostState = snackbarHostState,
                        )
                    } else {
                        RecieverMessage(
                            item = item,
                            timestamp = timestamp,
                            context = context,
                            chatViewModel = chatViewModel,
                            chatId = args.chatId,
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            reactionMsgSheetState = reactionMsgSheetState,
                            index = index,
                            messagesList = messagesList,
                            lazyListState = lazyListState,
                            chat = chat,
                            currentUser = currentUser,
                            chatsSettings = chatsSettings,
                        )
                    }
                }
            }

            // top Message Date
            topMessageDate?.let {
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .align(alignment = Alignment.TopCenter),
                    visible = lazyListState.isScrollInProgress
                ) {
                    Card(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(5.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSystemInDarkTheme()) Color.DarkGray else SearchBarColorLight,
                            contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(all = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier,
                                text = it,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

        }

        if (isDeleteMsgDialogOpen) {
            deleteMessagesDialog(
                onDeleteForSender = {
                    scope.launch(Dispatchers.Main) {
                        chatViewModel.deleteMsgForSender(
                            args.chatId,
                            chatViewModel.selectedSenderItemsIds as List<String>
                        ).collect { result ->
                            when (result) {
                                is ResultState.Failure -> {
                                    isDeleteMsgDialogOpen = false
                                    isMsgsDeleted = true
                                    snackbarHostState.showSnackbar(
                                        message = result.error.message.toString(),
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                ResultState.Loading -> {
                                    isDeleteMsgDialogOpen = false
                                    isMsgsDeleted = false
                                }

                                is ResultState.Success -> {
                                    chatViewModel.updateLastMessageOfChat(
                                        chatId = args.chatId,
                                        senderLastMsg = "You Deleted This Message",
                                        receiverLastMsg = null,
                                        lastMsgImageUrl = null,
                                        timestamp = null
                                    )
                                    isDeleteMsgDialogOpen = false
                                    isMsgsDeleted = true
                                    chatViewModel.isMessageSelected.value = false
                                    chatViewModel.selectedSenderItemsIds.clear()
                                    chatViewModel.selectedReceiverItemsIds.clear()
                                }

                                is ResultState.Progress -> TODO()
                            }
                        }
                    }
                },
                onDeleteForEveryone = {
                    scope.launch(Dispatchers.Main) {
                        chatViewModel.deleteMsgForEveryone(
                            args.chatId,
                            chatViewModel.selectedSenderItemsIds as List<String>
                        ).collect { result ->
                            when (result) {
                                is ResultState.Failure -> {
                                    isDeleteMsgDialogOpen = false
                                    isMsgsDeleted = true
                                    snackbarHostState.showSnackbar(
                                        message = result.error.message.toString(),
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                ResultState.Loading -> {
                                    isDeleteMsgDialogOpen = false
                                    isMsgsDeleted = false
                                }

                                is ResultState.Success -> {
                                    chatViewModel.updateLastMessageOfChat(
                                        chatId = args.chatId,
                                        senderLastMsg = "You Deleted This Message",
                                        receiverLastMsg = "Message Deleted",
                                        lastMsgImageUrl = null,
                                        timestamp = Timestamp.now()
                                    )
                                    isDeleteMsgDialogOpen = false
                                    isMsgsDeleted = true
                                    chatViewModel.isMessageSelected.value = false
                                    chatViewModel.selectedSenderItemsIds.clear()
                                    chatViewModel.selectedReceiverItemsIds.clear()
                                }

                                is ResultState.Progress -> TODO()
                            }
                        }
                    }
                },
                onDismiss = {
                    isDeleteMsgDialogOpen = false
                }
            )
        }

        if (isCameraAndGalleryPickDialogOpen) {
            PickImageFromCameraAndGalleryDialog(
                onCameraClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onGalleryClick = {
                    galleyLauncher.launch("image/*")
                },
                onDismiss = {
                    isCameraAndGalleryPickDialogOpen = false
                }
            )
        }

        if (reactionMsgSheetState.isVisible) {
            chatViewModel.msgReactionItem.value?.let {
                MessageReactionBottomSheet(
                    reactionMsgSheetState = reactionMsgSheetState,
                    messageModel = it,
                    chatViewModel = chatViewModel,
                    chatId = args.chatId,
                    scope = scope,
                    chat = chat,
                    currentUser = currentUser,
                    snackbarHostState = snackbarHostState
                )
            }
        }

        if (!isMsgsDeleted) {
            LoadingDialog()
        }

        if (!isImageMessageSended) {

            val animatedProgress = animateFloatAsState(
                targetValue = progress,
                animationSpec = if (!isImageMessageSended)
                    tween(durationMillis = 1000) else snap()
            )

            CircularProgressIndicatorWithValue(progress = animatedProgress.value)
        }

        if (!isMessageSended) {
            messagesList.toMutableList().add(
                MessageModel(
                    senderId = currentUser?.key ?: "",
                    receiverId = args.chatId,
                    message = message,
                    imageUrl = null,
                    timeStamp = Timestamp.now(),
                    monthName = "",
                    seen = false,
                    sended = false,
                    deleted = false,
                    key = "1",
                    repliedMessage = repliedMessageByIdWhenDragComplete?.message?.let {
                        it
                    } ?: "",
                    repliedMessageImageUrl = repliedMessageByIdWhenDragComplete?.imageUrl?.let {
                        it
                    } ?: "",
                    repliedMessageKey = chatViewModel.repliedMsgItemId.value?.let {
                        it
                    } ?: "",
                    repliedMessageStatus = if (
                        repliedMessageByIdWhenDragComplete?.senderId == currentUser?.key
                    ) "You" else chat?.phoneNumber
                )
            )
        }

        if (isImageFromCameraOrGalleryClicked) {
            FullScreenImageDialog(
                imageString = "",
                imageBitmap = galleryImageBitmap.value?.let {
                    it
                } ?: cameraImageBitmap.value!!,
                imageBy = "",
                scale = fullScreenImageFromCameraOrGalleyScale.value,
                onDismiss = {
                    isImageFromCameraOrGalleryClicked = false
                }
            )
        }


    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SenderMessage(
    item: MessageModel?,
    index: Int,
    timestamp: Timestamp?,
    chatViewModel: ChatViewModel,
    messagesList: List<MessageModel?>,
    chatId: String,
    context: Context,
    scope: CoroutineScope,
    reactionMsgSheetState: SheetState,
    chat: ChatModel?,
    currentUser: UserModel?,
    chatsSettings: SettingsChatsModel?,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
) {

    if (chatViewModel.selectedSenderItemsIds.size == 1 && chatViewModel.selectedReceiverItemsIds.size == 0) {
        chatViewModel.getMessageById(
            chatId,
            chatViewModel.selectedSenderItemsIds.firstOrNull() ?: ""
        )
    }

    var isReactionMenuVisible by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    var offsetX = remember { Animatable(0f) }
    val maxWidth = 150.dp
    var isDragging by remember { mutableStateOf(false) }

    var isMsgImageClicked by remember { mutableStateOf(false) }
    var fullScreenMsgImageScale = remember { Animatable(0f) }

    if (isMsgImageClicked) {
        scope.launch {
            fullScreenMsgImageScale.animateTo(1f)
        }
    } else {
        scope.launch {
            fullScreenMsgImageScale.animateTo(0f)
        }
    }


    val clickedRepliedMsgContainerColor by animateColorAsState(
        targetValue = if (item?.key == chatViewModel.clickedRepliedMsgKey.value) {
            if (isSystemInDarkTheme()) Green
            else LowGreenLight
        } else Color.Unspecified,
        animationSpec = tween(durationMillis = 500)
    )

    val selectedMsgBgColor by animateColorAsState(
        targetValue = if (chatViewModel.selectedSenderItemsIds.contains(messagesList[index]?.key)) {
            if (isSystemInDarkTheme()) Green
            else LowGreenLight
        } else Color.Unspecified,
        animationSpec = tween(durationMillis = 500)
    )


    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
                .clip(
                    shape = RoundedCornerShape(
                        size = if (chatViewModel.selectedSenderItemsIds.contains(
                                messagesList[index]?.key
                            )
                        ) 20.dp else 0.dp
                    )
                )
                .background(color = selectedMsgBgColor)
                .background(color = clickedRepliedMsgContainerColor),
        ) {
            Row(
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDragging) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(all = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterStart),
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Sender Message Reply Icon"
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        // .align(alignment = Alignment.CenterEnd)
                        .offset {
                            IntOffset(offsetX.value.roundToInt(), 0)
                        }
                        .pointerInput(true) {
                            if (item?.deleted == false) {
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        if (offsetX.value == maxWidth.toPx() - 100.dp.toPx()) {
                                            scope.launch {
                                                offsetX.animateTo(0f)
                                            }
                                            chatViewModel.repliedMsgItemId.value = item?.key
                                        } else {
                                            scope.launch {
                                                offsetX.animateTo(0f)
                                            }
                                        }
                                    }
                                ) { change, dragAmount ->
                                    change.consumeAllChanges()
                                    val newOffsetX = offsetX.value + dragAmount
                                    scope.launch {
                                        offsetX.snapTo(
                                            newOffsetX.coerceIn(
                                                0f,
                                                maxWidth.toPx() - 100.dp.toPx()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(
                                    min = 50.dp,
                                )
                                .heightIn(
                                    min = 50.dp
                                )
                                .padding(all = if (item?.reaction == null) 10.dp else 5.dp)
                                .padding(start = if (!item?.imageUrl.isNullOrEmpty()) 70.dp else 30.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                                .indication(
                                    interactionSource = interactionSource,
                                    LocalIndication.current
                                )
                                .onSizeChanged {
                                    itemHeight = with(density) { it.height.toDp() }
                                }
                                .pointerInput(true) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (item?.deleted == false) {
                                                if (!chatViewModel.selectedSenderItemsIds.contains(
                                                        messagesList[index]?.key
                                                    )
                                                ) {
                                                    chatViewModel.selectedSenderItemsIds.add(
                                                        messagesList[index]?.key
                                                    )
                                                    chatViewModel.isMessageSelected.value = true
                                                }
                                                isReactionMenuVisible = true
                                                pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                            }
                                        },
                                        onPress = {
                                            if (item?.deleted == false) {
                                                val press = PressInteraction.Press(it)
                                                interactionSource.emit(press)
                                                tryAwaitRelease()
                                                interactionSource.emit(
                                                    PressInteraction.Release(
                                                        press
                                                    )
                                                )
                                            }
                                        },
                                        onTap = {
                                            if (chatViewModel.selectedSenderItemsIds.size != 0 || chatViewModel.selectedReceiverItemsIds.size != 0) {
                                                if (item?.deleted == false) {
                                                    if (!chatViewModel.selectedSenderItemsIds.contains(
                                                            messagesList[index]?.key
                                                        )
                                                    ) {
                                                        chatViewModel.selectedSenderItemsIds.add(
                                                            messagesList[index]?.key
                                                        )
                                                        chatViewModel.isMessageSelected.value = true
                                                    } else {
                                                        chatViewModel.selectedSenderItemsIds.remove(
                                                            messagesList[index]?.key
                                                        )
                                                    }
                                                }
                                            }

                                            if (chatViewModel.selectedSenderItemsIds.size == 0 && chatViewModel.selectedReceiverItemsIds.size == 0) {
                                                chatViewModel.isMessageSelected.value = false
                                            }
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystemInDarkTheme()) LowGreenDark else Green
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(all = if (item?.imageUrl.isNullOrEmpty()) 10.dp else 5.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                ) {
                                    if (item?.deleted == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                modifier = Modifier
                                                    .width(15.dp)
                                                    .height(15.dp),
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = if (isSystemInDarkTheme()) Color.LightGray else SearchBarColorLight
                                            )
                                            Text(
                                                text = "You Deleted This Message",
                                                style = TextStyle(
                                                    fontSize = if (chatsSettings?.fontSize == "Small")
                                                        FontSize.SMALL.sp
                                                    else if (chatsSettings?.fontSize == "Large")
                                                        FontSize.LARGE.sp
                                                    else FontSize.MEDIUM.sp,
                                                    fontStyle = FontStyle.Italic,
                                                    color = if (isSystemInDarkTheme()) Color.LightGray else SearchBarColorLight,
                                                )
                                            )
                                        }
                                    } else {
                                        Column {
                                            if (!item?.repliedMessageKey.isNullOrEmpty()) {
                                                Card(
                                                    modifier = Modifier
                                                        .widthIn(min = 50.dp)
                                                        .padding(bottom = if (item?.imageUrl.isNullOrEmpty()) 0.dp else 5.dp)
                                                        .pointerInput(true) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    val repliedMsgIndex =
                                                                        messagesList.indexOfFirst { index ->
                                                                            index?.key == item?.repliedMessageKey ?: ""
                                                                        }
                                                                    scope.launch {
                                                                        repliedMsgIndex?.let {
                                                                            lazyListState.animateScrollToItem(
                                                                                it
                                                                            )
                                                                        } ?: 0
                                                                        chatViewModel.clickedRepliedMsgKey.value =
                                                                            item?.repliedMessageKey
                                                                                ?: ""
                                                                        delay(1000L)
                                                                        chatViewModel.clickedRepliedMsgKey.value =
                                                                            ""
                                                                    }
                                                                }
                                                            )
                                                        },
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
                                                                    text = item?.repliedMessageStatus
                                                                        ?: "",
                                                                    style = TextStyle(
                                                                        fontSize = 15.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (item?.repliedMessageStatus == "You") LowGreenLight else Color.Black
                                                                    )
                                                                )
                                                                if (item?.message.isNullOrEmpty()) {
                                                                    if (!item?.imageUrl.isNullOrEmpty()) {
                                                                        Text(
                                                                            text = "Photo",
                                                                            style = TextStyle(
                                                                                fontSize = 12.sp
                                                                            )
                                                                        )
                                                                    }
                                                                } else {
                                                                    Text(
                                                                        text = item?.repliedMessage
                                                                            ?: "",
                                                                        style = TextStyle(
                                                                            fontSize = 12.sp
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                            if (!item?.imageUrl.isNullOrEmpty()) {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                            if (!item?.repliedMessageImageUrl.isNullOrEmpty()) {
                                                                AsyncImage(
                                                                    modifier = Modifier
                                                                        .width(35.dp)
                                                                        .height(35.dp)
                                                                        .clip(
                                                                            shape = RoundedCornerShape(
                                                                                10.dp
                                                                            )
                                                                        ),
                                                                    model = item?.repliedMessageImageUrl
                                                                        ?: "",
                                                                    contentDescription = null,
                                                                    contentScale = ContentScale.FillWidth,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (!item?.imageUrl.isNullOrEmpty()) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(
                                                            max = 300.dp
                                                        )
                                                        .pointerInput(true) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    isMsgImageClicked = true
                                                                }
                                                            )
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color.Transparent
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                    ) {
                                                        if (!isMsgImageClicked) {
                                                            SubcomposeAsyncImage(
                                                                modifier = Modifier
                                                                    .fillMaxSize(),
                                                                model = item?.imageUrl ?: "",
                                                                contentDescription = "Sender Image Message",
                                                                contentScale = ContentScale.FillWidth,
                                                                loading = {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxSize(),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        CircularProgressIndicator(
                                                                            modifier = Modifier
                                                                                .wrapContentSize(),
                                                                            color = Color.White
                                                                        )
                                                                    }
                                                                },
                                                                error = {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Error,
                                                                        contentDescription = "Error Image Message"
                                                                    )
                                                                },
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Text(
                                                modifier = Modifier
                                                    .padding(
                                                        start = if (
                                                            !item?.imageUrl.isNullOrEmpty() ||
                                                            !item?.repliedMessageKey.isNullOrEmpty()
                                                        ) 5.dp else 0.dp,

                                                        top = if (
                                                            !item?.imageUrl.isNullOrEmpty() ||
                                                            !item?.repliedMessageKey.isNullOrEmpty()
                                                        ) 5.dp else 0.dp
                                                    ),
                                                text = item?.message ?: "",
                                                style = TextStyle(
                                                    fontSize = if (
                                                        !item?.message.isNullOrEmpty()
                                                    ) {
                                                        if (chatsSettings?.fontSize == "Small")
                                                            FontSize.SMALL.sp
                                                        else if (chatsSettings?.fontSize == "Large")
                                                            FontSize.LARGE.sp
                                                        else FontSize.MEDIUM.sp
                                                    } else 0.sp,
                                                    color = Color.White,
                                                )
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .align(alignment = Alignment.End),
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .padding(start = 10.dp),
                                            text = formatTimestampToAmPm(timestamp),
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        )
                                        if (item?.deleted == false) {
                                            Icon(
                                                modifier = Modifier
                                                    .width(15.dp)
                                                    .height(15.dp),
                                                imageVector = if (item?.sended == true)
                                                    Icons.Default.Check
                                                else Icons.Default.Update,
                                                contentDescription = "Message status iconLike is sended or not"
                                            )
                                        }
                                    }
                                }

                                MessageReactionDropdownMenu(
                                    isReactionMenuVisible = isReactionMenuVisible,
                                    item = item,
                                    chatId = chatId,
                                    chatViewModel = chatViewModel,
                                    onDismiss = {
                                        isReactionMenuVisible = false
                                    },
                                    pressOffset = pressOffset,
                                    itemHeight = itemHeight,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope
                                )
                            }
                        }
                    }

                    if (isMsgImageClicked) {
                        FullScreenMessageImageDialog(
                            item = item,
                            messageBy = "You",
                            chatViewModel = chatViewModel,
                            chatId = chatId,
                            chat = chat,
                            currentUser = currentUser,
                            scale = fullScreenMsgImageScale.value,
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            onDismiss = {
                                isMsgImageClicked = false
                            }
                        )
                    }

                    if (item?.reaction != null && item?.reaction?.size != 0) {
                        Column(
                            modifier = Modifier
                                .widthIn(25.dp)
                                .heightIn(25.dp)
                                .padding(end = 10.dp)
                                .align(alignment = Alignment.End)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape = RoundedCornerShape(30.dp))
                                    .clickable {
                                        chatViewModel.msgReactionItem.value = item
                                        scope.launch {
                                            reactionMsgSheetState.show()
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color.DarkGray.copy(
                                        0.8f
                                    ) else Color.White.copy(0.8f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(all = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {

                                        val reaction = item?.reaction?.last()

                                        Text(
                                            text = reaction?.reaction.toString(),
                                            style = TextStyle(
                                                fontSize = 12.sp
                                            )
                                        )

                                        item?.reaction?.size?.let {
                                            if (it > 1) {
                                                Text(
                                                    text = it.toString(),
                                                    style = TextStyle(
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RecieverMessage(
    item: MessageModel?,
    timestamp: Timestamp?,
    context: Context,
    chatViewModel: ChatViewModel,
    chatId: String,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    reactionMsgSheetState: SheetState,
    index: Int,
    messagesList: List<MessageModel?>,
    lazyListState: LazyListState,
    chat: ChatModel?,
    currentUser: UserModel?,
    chatsSettings: SettingsChatsModel?,
) {

    if (chatViewModel.selectedReceiverItemsIds.size == 1 && chatViewModel.selectedSenderItemsIds.size == 0) {
        chatViewModel.getMessageById(
            chatId,
            chatViewModel.selectedReceiverItemsIds.firstOrNull() ?: ""
        )
    }


    var isReactionMenuVisible by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    var offsetX = remember { Animatable(0f) }
    val maxWidth = 150.dp
    var isDragging by remember { mutableStateOf(false) }

    var isMsgImageClicked by remember { mutableStateOf(false) }
    var fullScreenMsgImageScale = remember { Animatable(0f) }

    if (isMsgImageClicked) {
        scope.launch {
            fullScreenMsgImageScale.animateTo(1f)
        }
    } else {
        scope.launch {
            fullScreenMsgImageScale.animateTo(0f)
        }
    }

    val clickedRepliedMsgContainerColor by animateColorAsState(
        targetValue = if (item?.key == chatViewModel.clickedRepliedMsgKey.value) {
            if (isSystemInDarkTheme()) Green
            else LowGreenLight
        } else Color.Unspecified,
        animationSpec = tween(durationMillis = 500)
    )

    val selectedMsgBgColor by animateColorAsState(
        targetValue = if (chatViewModel.selectedReceiverItemsIds.contains(messagesList[index]?.key)) {
            if (isSystemInDarkTheme()) Green
            else LowGreenLight
        } else Color.Unspecified,
        animationSpec = tween(durationMillis = 500)
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
                .clip(
                    shape = RoundedCornerShape(
                        size = if (chatViewModel.selectedReceiverItemsIds.contains(
                                messagesList[index]?.key
                            )
                        ) 20.dp else 0.dp
                    )
                )
                .background(color = selectedMsgBgColor)
                .background(color = clickedRepliedMsgContainerColor),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDragging) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(all = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(alignment = Alignment.CenterStart),
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Receiver Message Reply Icon"
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .offset {
                            IntOffset(offsetX.value.roundToInt(), 0)
                        }
                        .pointerInput(true) {
                            if (item?.deleted == false) {
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        if (offsetX.value == maxWidth.toPx() - 100.dp.toPx()) {
                                            scope.launch {
                                                offsetX.animateTo(0f)
                                            }
                                            chatViewModel.repliedMsgItemId.value = item?.key
                                        } else {
                                            scope.launch {
                                                offsetX.animateTo(0f)
                                            }
                                        }
                                    }
                                ) { change, dragAmount ->
                                    if (isDragging) {
                                        change.consumeAllChanges()
                                        val newOffsetX = offsetX.value + dragAmount
                                        scope.launch {
                                            offsetX.snapTo(
                                                newOffsetX.coerceIn(
                                                    0f,
                                                    maxWidth.toPx() - 100.dp.toPx()
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(
                                    min = 50.dp
                                )
                                .heightIn(
                                    min = 50.dp
                                )
                                .padding(all = if (item?.reaction == null) 10.dp else 5.dp)
                                .padding(end = if (!item?.imageUrl.isNullOrEmpty()) 70.dp else 30.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                                .onSizeChanged {
                                    itemHeight = with(density) { it.height.toDp() }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
                                contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(
                                        all = if (item?.imageUrl.toString()
                                                .isEmpty()
                                        ) 10.dp else 5.dp
                                    )
                                    .indication(
                                        interactionSource = interactionSource,
                                        LocalIndication.current
                                    )
                                    .pointerInput(true) {
                                        detectTapGestures(
                                            onLongPress = {
                                                if (item?.deleted == false) {
                                                    if (!chatViewModel.selectedReceiverItemsIds.contains(
                                                            messagesList[index]?.key
                                                        )
                                                    ) {
                                                        chatViewModel.selectedReceiverItemsIds.add(
                                                            messagesList[index]?.key
                                                        )
                                                        chatViewModel.isMessageSelected.value = true
                                                    }
                                                    isReactionMenuVisible = true
                                                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                                }
                                            },
                                            onPress = {
                                                if (item?.deleted == false) {
                                                    val press = PressInteraction.Press(it)
                                                    interactionSource.emit(press)
                                                    tryAwaitRelease()
                                                    interactionSource.emit(
                                                        PressInteraction.Release(
                                                            press
                                                        )
                                                    )
                                                }
                                            },
                                            onTap = {
                                                if (chatViewModel.selectedReceiverItemsIds.size != 0 || chatViewModel.selectedSenderItemsIds.size != 0) {
                                                    if (item?.deleted == false) {
                                                        if (!chatViewModel.selectedReceiverItemsIds.contains(
                                                                messagesList[index]?.key
                                                            )
                                                        ) {
                                                            chatViewModel.selectedReceiverItemsIds.add(
                                                                messagesList[index]?.key
                                                            )
                                                            chatViewModel.isMessageSelected.value =
                                                                true
                                                        } else {
                                                            chatViewModel.selectedReceiverItemsIds.remove(
                                                                messagesList[index]?.key
                                                            )
                                                        }
                                                    }
                                                }

                                                if (chatViewModel.selectedReceiverItemsIds.size == 0 && chatViewModel.selectedSenderItemsIds.size == 0) {
                                                    chatViewModel.isMessageSelected.value = false
                                                }
                                            }
                                        )
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                ) {
                                    if (item?.deleted == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                modifier = Modifier
                                                    .width(15.dp)
                                                    .height(15.dp),
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray
                                            )
                                            Text(
                                                text = "This Message Was Deleted",
                                                style = TextStyle(
                                                    fontSize = if (chatsSettings?.fontSize == "Small")
                                                        FontSize.SMALL.sp
                                                    else if (chatsSettings?.fontSize == "Large")
                                                        FontSize.LARGE.sp
                                                    else FontSize.MEDIUM.sp,
                                                    fontStyle = FontStyle.Italic,
                                                    color = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray,
                                                )
                                            )
                                        }
                                    } else {
                                        Column {
                                            if (!item?.repliedMessageKey.isNullOrEmpty()) {
                                                Card(
                                                    modifier = Modifier
                                                        .widthIn(min = 50.dp)
                                                        .padding(bottom = if (item?.imageUrl.isNullOrEmpty()) 0.dp else 5.dp)
                                                        .pointerInput(true) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    val repliedMsgIndex =
                                                                        messagesList.indexOfFirst { index ->
                                                                            index?.key == item?.repliedMessageKey ?: ""
                                                                        }
                                                                    scope.launch {
                                                                        repliedMsgIndex?.let {
                                                                            lazyListState.animateScrollToItem(
                                                                                it
                                                                            )
                                                                        } ?: 0
                                                                        chatViewModel.clickedRepliedMsgKey.value =
                                                                            item?.repliedMessageKey
                                                                                ?: ""
                                                                        delay(1000L)
                                                                        chatViewModel.clickedRepliedMsgKey.value =
                                                                            ""
                                                                    }
                                                                }
                                                            )
                                                        },
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
                                                                    text = item?.repliedMessageStatus
                                                                        ?: "",
                                                                    style = TextStyle(
                                                                        fontSize = 15.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (item?.repliedMessageStatus == "You") Green else Color.Black
                                                                    )
                                                                )
                                                                if (item?.message.isNullOrEmpty()) {
                                                                    if (!item?.imageUrl.isNullOrEmpty()) {
                                                                        Text(
                                                                            text = "Photo",
                                                                            style = TextStyle(
                                                                                fontSize = 12.sp
                                                                            )
                                                                        )
                                                                    }
                                                                } else {
                                                                    Text(
                                                                        text = item?.repliedMessage
                                                                            ?: "",
                                                                        style = TextStyle(
                                                                            fontSize = 12.sp
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                            if (!item?.imageUrl.isNullOrEmpty()) {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                            if (!item?.repliedMessageImageUrl.isNullOrEmpty()) {
                                                                AsyncImage(
                                                                    modifier = Modifier
                                                                        .width(35.dp)
                                                                        .height(35.dp)
                                                                        .clip(
                                                                            shape = RoundedCornerShape(
                                                                                10.dp
                                                                            )
                                                                        ),
                                                                    model = item?.repliedMessageImageUrl
                                                                        ?: "",
                                                                    contentDescription = null,
                                                                    contentScale = ContentScale.FillWidth,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (!item?.imageUrl.isNullOrEmpty()) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(
                                                            max = 300.dp
                                                        )
                                                        .pointerInput(true) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    isMsgImageClicked = true
                                                                }
                                                            )
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color.Transparent
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize(),
                                                    ) {
                                                        if (!isMsgImageClicked) {
                                                            SubcomposeAsyncImage(
                                                                modifier = Modifier
                                                                    .fillMaxSize(),
                                                                model = item?.imageUrl ?: "",
                                                                contentDescription = "Receiver Image Message",
                                                                contentScale = ContentScale.FillWidth,
                                                                loading = {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxSize(),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        CircularProgressIndicator(
                                                                            modifier = Modifier
                                                                                .wrapContentSize(),
                                                                        )
                                                                    }
                                                                },
                                                                error = {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Error,
                                                                        contentDescription = "Error Image Message"
                                                                    )
                                                                },
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Text(
                                                modifier = Modifier
                                                    .padding(
                                                        start = if (
                                                            !item?.imageUrl.isNullOrEmpty() ||
                                                            !item?.repliedMessageKey.isNullOrEmpty()
                                                        ) 5.dp else 0.dp,

                                                        top = if (
                                                            !item?.imageUrl.isNullOrEmpty() ||
                                                            !item?.repliedMessageKey.isNullOrEmpty()
                                                        ) 5.dp else 0.dp
                                                    ),
                                                text = item?.message ?: "",
                                                style = TextStyle(
                                                    fontSize = if (
                                                        !item?.message.isNullOrEmpty()
                                                    ) {
                                                        if (chatsSettings?.fontSize == "Small")
                                                            FontSize.SMALL.sp
                                                        else if (chatsSettings?.fontSize == "Large")
                                                            FontSize.LARGE.sp
                                                        else FontSize.MEDIUM.sp
                                                    } else 0.sp
                                                )
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .align(alignment = Alignment.End)
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .padding(start = 10.dp),
                                            text = formatTimestampToAmPm(timestamp),
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                            )
                                        )
                                    }
                                }

                                MessageReactionDropdownMenu(
                                    isReactionMenuVisible = isReactionMenuVisible,
                                    item = item,
                                    chatId = chatId,
                                    chatViewModel = chatViewModel,
                                    onDismiss = {
                                        isReactionMenuVisible = false
                                    },
                                    pressOffset = pressOffset,
                                    itemHeight = itemHeight,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope
                                )

                            }
                        }
                    }

                    if (isMsgImageClicked) {
                        FullScreenMessageImageDialog(
                            item = item,
                            messageBy = if (chat?.usernameGivenByChatCreator.isNullOrEmpty())
                                chat?.phoneNumber
                                    ?: "" else chat?.usernameGivenByChatCreator
                                ?: "",
                            chatViewModel = chatViewModel,
                            chatId = chatId,
                            chat = chat,
                            currentUser = currentUser,
                            scale = fullScreenMsgImageScale.value,
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            onDismiss = {
                                isMsgImageClicked = true
                            }
                        )
                    }


                    if (item?.reaction != null && item?.reaction?.size != 0) {
                        Column(
                            modifier = Modifier
                                .widthIn(25.dp)
                                .heightIn(25.dp)
                                .padding(start = 10.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape = RoundedCornerShape(30.dp))
                                    .clickable {
                                        chatViewModel.msgReactionItem.value = item
                                        scope.launch {
                                            reactionMsgSheetState.show()
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color.DarkGray.copy(
                                        0.8f
                                    ) else Color.White.copy(0.8f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(all = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        val reaction = item?.reaction?.last()

                                        Text(
                                            text = reaction?.reaction.toString(),
                                            style = TextStyle(
                                                fontSize = 12.sp
                                            )
                                        )

                                        item?.reaction?.size?.let {
                                            if (it > 1) {
                                                Text(
                                                    text = it.toString(),
                                                    style = TextStyle(
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun deleteMessagesDialog(
    onDeleteForSender: () -> Unit,
    onDeleteForEveryone: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = {
                        onDeleteForSender.invoke()
                        onDismiss.invoke()
                    }) {
                        Text(text = "Delete For Me.")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = {
                        onDeleteForEveryone.invoke()
                        onDismiss.invoke()
                    }) {
                        Text(text = "Delete For Everyone.")
                    }
                }
            }
        }
    )
}

@Composable
fun MessageReactionBottomSheet(
    reactionMsgSheetState: SheetState,
    messageModel: MessageModel?,
    chatViewModel: ChatViewModel,
    chatId: String,
    scope: CoroutineScope,
    chat: ChatModel?,
    currentUser: UserModel?,
    snackbarHostState: SnackbarHostState
) {
    ModalBottomSheet(
        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
        sheetState = reactionMsgSheetState,
        onDismissRequest = {
            scope.launch {
                reactionMsgSheetState.hide()
            }
        },
        content = {
            LazyColumn(
                modifier = Modifier
                    .padding(bottom = 100.dp),
                reverseLayout = true
            ) {

                messageModel?.reaction?.let {
                    items(it.size) { index ->

                        val item = messageModel.reaction[index]

                        MessageReactionsItem(
                            item = item,
                            messageModel = messageModel,
                            currentUser = currentUser,
                            chatViewModel = chatViewModel,
                            chatId = chatId,
                            reactionMsgSheetState = reactionMsgSheetState,
                            chat = chat,
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            index = index
                        )

                    }
                }

            }
        }
    )
}

@Composable
fun MessageReactionsItem(
    item: MessageReactionModel?,
    messageModel: MessageModel?,
    currentUser: UserModel?,
    chatViewModel: ChatViewModel,
    chatId: String,
    reactionMsgSheetState: SheetState,
    chat: ChatModel?,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    index: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 20.dp)
            .clickable {
                if (item?.reactionAddedBy == currentUser?.key) {
                    scope.launch(Dispatchers.Main) {
                        reactionMsgSheetState.hide()
                        chatViewModel.deleteMessageReaction(
                            chatId = chatId,
                            messageKey = messageModel?.key ?: "",
                            reactionIndex = index,
                            reactionList = messageModel?.reaction ?: emptyList()
                        ).collect { result ->
                            when (result) {
                                is ResultState.Failure -> {
                                    chatViewModel.isMsgReactionDeleted.value = true
                                    snackbarHostState.showSnackbar(
                                        message = result.error.message.toString(),
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                ResultState.Loading -> {
                                    chatViewModel.isMsgReactionDeleted.value = false
                                }

                                is ResultState.Progress -> TODO()
                                is ResultState.Success -> {
                                    chatViewModel.updateLastMessageOfChat(
                                        chatId = chatId,
                                        senderLastMsg = null,
                                        receiverLastMsg = null,
                                        lastMsgImageUrl = null,
                                        timestamp = null
                                    )
                                    chatViewModel.isMsgReactionDeleted.value = true
                                }
                            }
                        }
                    }
                }
            },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(shape = RoundedCornerShape(30.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (item?.reactionAddedBy == currentUser?.key) {
                    if (currentUser?.profilePic.isNullOrEmpty()) { // here i need to add condition of currentUser profilePic
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
                            model = currentUser?.profilePic ?: "",
                            contentDescription = "Chat Profile Pic",
                            contentScale = ContentScale.FillWidth,
                            loading = {
                                Icon(
                                    modifier = Modifier
                                        .align(alignment = Alignment.Center),
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Loading Chat Image",
                                    tint = Color.White,
                                )
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
                } else {
                    if (chat?.profilePic.isNullOrEmpty()) { // here i need to add condition of currentUser profilePic
                        Icon(
                            modifier = Modifier
                                .align(alignment = Alignment.Center),
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    } else {
                        SubcomposeAsyncImage(
                            model = chat?.profilePic ?: "",
                            contentDescription = "Chat Profile Pic",
                            contentScale = ContentScale.FillWidth,
                            loading = {
                                Icon(
                                    modifier = Modifier
                                        .align(alignment = Alignment.Center),
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Loading User Image",
                                    tint = Color.White,
                                )
                            },
                            error = {
                                Icon(
                                    modifier = Modifier
                                        .align(alignment = Alignment.Center),
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error User Image",
                                    tint = Color.White,
                                )
                            }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                modifier = Modifier,// .weight(1f),
                text = if (item?.reactionAddedBy == currentUser?.key) "You" else {
                    if (chat?.usernameGivenByChatCreator.isNullOrEmpty())
                        chat?.phoneNumber ?: ""
                    else chat?.usernameGivenByChatCreator ?: ""
                },
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item?.reactionAddedBy == currentUser?.key) {
                Text(
                    text = "Tap to Remove",
                    style = TextStyle(
                        fontSize = 12.sp,
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.padding(top = 10.dp),
        ) {
            Text(
                modifier = Modifier
                    .align(alignment = Alignment.End),
                text = item?.reaction.toString(),
                style = TextStyle(
                    fontSize = 15.sp
                ),
            )

            Text(
                text = formatTimestampToAmPm(item?.timeStamp),
                style = TextStyle(
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun MessageReactionDropdownMenu(
    isReactionMenuVisible: Boolean,
    item: MessageModel?,
    chatId: String,
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit,
    pressOffset: DpOffset,
    itemHeight: Dp,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    DropdownMenu(
        modifier = Modifier
            .background(color = if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
        expanded = isReactionMenuVisible,
        onDismissRequest = {
            onDismiss.invoke()
        },
        offset = pressOffset.copy(
            y = pressOffset.y - itemHeight
        ),
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = true
        )
    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MessageReactionMenuList().forEach { reaction ->
                Row(
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .background(color = Color.Transparent)
                        .clickable {
                            onDismiss.invoke()
                            scope.launch(Dispatchers.Main) {
                                chatViewModel.onMessageReactionAdd(
                                    chatId,
                                    item?.key ?: "",
                                    reaction.toString()
                                ).collect { result ->
                                    when (result) {
                                        is ResultState.Failure -> {
                                            snackbarHostState.showSnackbar(
                                                message = result.error.message.toString(),
                                                duration = SnackbarDuration.Short
                                            )
                                            chatViewModel.isMsgReactionAdded.value =
                                                true
                                        }

                                        ResultState.Loading -> {
                                            chatViewModel.isMsgReactionAdded.value =
                                                false
                                        }

                                        is ResultState.Success -> {
                                            chatViewModel.isMessageSelected.value =
                                                false
                                            chatViewModel.selectedReceiverItemsIds.clear()
                                            chatViewModel.selectedSenderItemsIds.clear()

                                            chatViewModel.updateLastMessageOfChat(
                                                chatId = chatId,
                                                senderLastMsg = "You Reacted" + " '$reaction' " + "to " + if (
                                                    !item?.imageUrl.isNullOrEmpty()
                                                ) "Photo" else item?.message,

                                                receiverLastMsg = "You Reacted" + " '$reaction' " + "to " + if (
                                                    !item?.imageUrl.isNullOrEmpty()
                                                ) "Your Photo" else item?.message,

                                                lastMsgImageUrl = if (!item?.imageUrl.isNullOrEmpty())
                                                    item?.imageUrl
                                                else null,
                                                timestamp = Timestamp.now()
                                            )
                                            snackbarHostState.showSnackbar(
                                                message = result.data,
                                                duration = SnackbarDuration.Short
                                            )
                                            chatViewModel.isMsgReactionAdded.value =
                                                true
                                        }

                                        is ResultState.Progress -> TODO()
                                    }
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = reaction)
                }
            }
        }
    }
}

@Composable
fun MoreAboutSelectedMessageDropDownMenu(
    isMoreAboutSelectedMsgMenuVisible: Boolean,
    selectedReceiverMsgSize: Int,
    onInfoClicked: () -> Unit,
    onCopyClicked: () -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .background(color = if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
        expanded = isMoreAboutSelectedMsgMenuVisible,
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
        if (selectedReceiverMsgSize == 0) {
            DropdownMenuItem(
                text = {
                    Text(text = "Info")
                },
                onClick = {
                    onInfoClicked.invoke()
                    onDismiss.invoke()
                }
            )
        }

        DropdownMenuItem(
            text = {
                Text(text = "copy")
            },
            onClick = {
                onCopyClicked.invoke()
                onDismiss.invoke()
            }
        )

    }
}

@Composable
fun SingleChatScreenBottomBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onMessageEmpty: () -> Unit,
    chatId: String,
    chatViewModel: ChatViewModel,
    currentUser: UserModel?,
    galleryImageUri: Uri?,
    galleryImageBitmap: Bitmap?,
    cameraImageBitmap: Bitmap?,
    onCameraTrailingIconClicked: () -> Unit,
    onImageCancel: () -> Unit,
    chat: ChatModel?,
    replyMsgItem: MessageModel?,
    onReplyMsgContainerClicked: () -> Unit,
    focusRequester: FocusRequester,
    onTextMessageSuccess: () -> Unit,
    onTextMessageLoading: () -> Unit,
    onTextMessageFailure: (String) -> Unit,
    onImageUploadSuccess: () -> Unit,
    onImageUploadLoading: () -> Unit,
    onImageUploadFailure: (String) -> Unit,
    onImageUploadProgress: (Float) -> Unit, // take the progress value
    onImageMessageStores: (String) -> Unit, // take the key
    isImageMessageSended: Boolean,
    scope: CoroutineScope,
    isLandscape: Boolean,
    onSendIconClickWhenImageNotEmpty: () -> Unit,
    onImageFromCameraOrGalleryClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(
            visible = galleryImageUri != null ||
                    cameraImageBitmap != null ||
                    chatViewModel.repliedMsgItemId.value != null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    if (chatViewModel.repliedMsgItemId.value != null) {
                        // Replied Msg Container
                        Column(
                            modifier = Modifier
                                .heightIn(min = 30.dp, max = 300.dp)
                                .clip(
                                    shape = RoundedCornerShape(
                                        topStart = 25.dp,
                                        topEnd = 25.dp
                                    )
                                )
                                .background(color = if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(30.dp))
                                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                                    .pointerInput(true) {
                                        detectTapGestures(
                                            onTap = {
                                                onReplyMsgContainerClicked.invoke()
                                            }
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) LowGreenDark else Color.LightGray,
                                    contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 10.dp)
                                ) {
                                    IconButton(
                                        modifier = Modifier
                                            .width(15.dp)
                                            .height(15.dp)
                                            .align(alignment = Alignment.TopEnd),
                                        onClick = {
                                            chatViewModel.repliedMsgItemId.value = null
                                        }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Cancel Image Icon"
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                        ) {
                                            if (replyMsgItem?.senderId == currentUser?.key) {
                                                Text(
                                                    text = "You",
                                                    style = TextStyle(
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = LowGreenLight
                                                )
                                            } else {
                                                Text(
                                                    text = if (chat?.usernameGivenByChatCreator.toString()
                                                            .isEmpty()
                                                    )
                                                        chat?.phoneNumber
                                                            ?: "" else chat?.usernameGivenByChatCreator
                                                        ?: "",
                                                    style = TextStyle(
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.Black
                                                )
                                            }
                                            if (replyMsgItem?.message.isNullOrEmpty()) {
                                                if (!replyMsgItem?.imageUrl.isNullOrEmpty()) {
                                                    Text(
                                                        text = "Photo",
                                                        style = TextStyle(
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = replyMsgItem?.message
                                                        ?: "",
                                                    style = TextStyle(
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (!replyMsgItem?.imageUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(40.dp)
                                                    .clip(shape = RoundedCornerShape(10.dp)),
                                                model = replyMsgItem?.imageUrl
                                                    ?: "",
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (galleryImageUri != null || cameraImageBitmap != null) {
                        Column(
                            modifier = Modifier
                                .height(height = if (isLandscape) 100.dp else 200.dp)
                                .clip(
                                    shape = RoundedCornerShape(
                                        topStart = if (chatViewModel.repliedMsgItemId.value == null)
                                            25.dp else 0.dp,
                                        topEnd = if (chatViewModel.repliedMsgItemId.value == null)
                                            25.dp else 0.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                )
                                .background(color = if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                // Image
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                                        .clip(shape = RoundedCornerShape(15.dp))
                                        .pointerInput(true) {
                                            detectTapGestures(
                                                onTap = {
                                                    onImageFromCameraOrGalleryClicked.invoke()
                                                }
                                            )
                                        },
                                    model = galleryImageBitmap?.let {
                                        it
                                    } ?: cameraImageBitmap,
                                    contentDescription = "Gallery Image",
                                    contentScale = ContentScale.FillWidth
                                )

                                IconButton(
                                    modifier = Modifier
                                        .width(25.dp)
                                        .height(25.dp)
                                        .padding(end = 10.dp, top = 10.dp)
                                        .align(alignment = Alignment.TopEnd),
                                    onClick = {
                                        onImageCancel.invoke()
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Cancel Image Icon",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                    }
                }
                IconButton(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(25.dp)),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                    ),
                    onClick = {},
                    enabled = false
                ) {

                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(
                        min = 30.dp,
                        max = 150.dp
                    )
                    .clip(
                        shape = RoundedCornerShape(
                            bottomStart = 25.dp, bottomEnd = 25.dp,
                            topStart = if (
                                galleryImageUri != null || cameraImageBitmap != null
                                || chatViewModel.repliedMsgItemId.value != null
                            ) 0.dp else 25.dp,
                            topEnd = if (
                                galleryImageUri != null || cameraImageBitmap != null
                                || chatViewModel.repliedMsgItemId.value != null
                            ) 0.dp else 25.dp
                        )
                    )
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedContainerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
                    unfocusedContainerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
                ),
                value = message,
                onValueChange = {
                    onMessageChange.invoke(it)
                },
                placeholder = {
                    Text(
                        text = if (
                            galleryImageUri != null || cameraImageBitmap != null
                        ) "Caption (optional)" else "Message"
                    )
                },
                singleLine = false,
                keyboardOptions = KeyboardOptions.Default,
                trailingIcon = {
                    IconButton(onClick = {
                        onCameraTrailingIconClicked.invoke()
                    }) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera Icon"
                        )
                    }
                },
            )
            IconButton(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(25.dp))
                    .background(color = Green)
                    .align(alignment = Alignment.Bottom),
                enabled = if (isImageMessageSended) true else false,
                onClick = {
                    if (galleryImageUri != null || cameraImageBitmap != null) {
                        onSendIconClickWhenImageNotEmpty.invoke()
                        scope.launch(Dispatchers.Main) {
                            chatViewModel.onImageMessageUpload(
                                imageBitmap = galleryImageBitmap?.let {
                                    galleryImageBitmap
                                } ?: cameraImageBitmap!!,

                                ).collect { result ->
                                when (result) {
                                    is ResultState.Failure -> {
                                        onImageUploadFailure.invoke(result.error.message.toString())
                                    }

                                    ResultState.Loading -> {
                                        onImageUploadLoading.invoke()
                                    }

                                    is ResultState.Progress -> {
                                        onImageUploadProgress.invoke(result.progress)
                                    }

                                    is ResultState.Success -> {

                                        if (message.isEmpty()) {
                                            chatViewModel.updateLastMessageOfChat(
                                                chatId = chatId,
                                                senderLastMsg = "You: Photo",
                                                receiverLastMsg = "Photo",
                                                lastMsgImageUrl = result.data.toString(),
                                                timestamp = Timestamp.now()
                                            )
                                        } else {
                                            chatViewModel.updateLastMessageOfChat(
                                                chatId = chatId,
                                                senderLastMsg = "You: $message",
                                                receiverLastMsg = message,
                                                lastMsgImageUrl = result.data.toString(),
                                                timestamp = Timestamp.now()
                                            )
                                        }

                                        chatViewModel.onImageMessageSent(
                                            result.data,
                                            message,
                                            chatId,
                                            repliedMessage = replyMsgItem?.message?.let {
                                                it
                                            } ?: "",
                                            repliedMessageImageUrl = replyMsgItem?.imageUrl?.let {
                                                it
                                            } ?: "",
                                            repliedMessageKey = chatViewModel.repliedMsgItemId.value?.let {
                                                it
                                            } ?: "",
                                            senderRepliedMessageStatus = if (
                                                replyMsgItem?.senderId == currentUser?.key
                                            ) "You" else chat?.phoneNumber,
                                            receiverRepliedMessageStatus = if (
                                                replyMsgItem?.senderId == currentUser?.key
                                            ) currentUser?.phoneNumber else "You",
                                            onImageMessageStores = { key ->
                                                onImageMessageStores.invoke(key)
                                            }
                                        )
                                        onImageUploadSuccess.invoke()
                                    }
                                }
                            }
                        }
                    } else {
                        if (message.isEmpty()) {
                            scope.launch {
                                delay(1000L)
                                onMessageEmpty.invoke()
                            }
                        } else {
                            scope.launch(Dispatchers.Main) {
                                chatViewModel.onTextMessageSent(
                                    message = message,
                                    chatId = chatId,
                                    repliedMessage = replyMsgItem?.message?.let {
                                        it
                                    } ?: "",
                                    repliedMessageImageUrl = replyMsgItem?.imageUrl?.let {
                                        it
                                    } ?: "",
                                    repliedMessageKey = chatViewModel.repliedMsgItemId.value?.let {
                                        it
                                    } ?: "",
                                    senderRepliedMessageStatus = if (
                                        replyMsgItem?.senderId == currentUser?.key
                                    ) "You" else chat?.phoneNumber,
                                    receiverRepliedMessageStatus = if (
                                        replyMsgItem?.senderId == currentUser?.key
                                    ) currentUser?.phoneNumber else "You",
                                ).collect {
                                    when (it) {
                                        is ResultState.Failure -> {
                                            onTextMessageFailure.invoke(it.error.message.toString())
                                        }

                                        ResultState.Loading -> {
                                            onTextMessageLoading.invoke()
                                        }

                                        is ResultState.Success -> {
                                            scope.launch {
                                                chatViewModel.updateLastMessageOfChat(
                                                    chatId = chatId,
                                                    senderLastMsg = "You: $message",
                                                    receiverLastMsg = message,
                                                    lastMsgImageUrl = null,
                                                    Timestamp.now()
                                                )
                                            }
                                            chatViewModel.updateMessageSended(
                                                chatId = chatId,
                                                messageKey = it.data.key,
                                                sended = true
                                            )
                                            onTextMessageSuccess.invoke()
                                        }

                                        is ResultState.Progress -> TODO()
                                    }
                                }
                            }
                        }
                    }

                }) {
                Icon(
                    modifier = Modifier
                        .padding(all = 10.dp),
                    imageVector = Icons.Default.Send,
                    tint = Color.White,
                    contentDescription = "Send Message Icon"
                )
            }
        }
    }
}