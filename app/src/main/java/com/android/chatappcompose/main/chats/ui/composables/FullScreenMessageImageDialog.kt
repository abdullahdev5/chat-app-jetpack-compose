@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.main.chats.ui.composables

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.android.chatappcompose.core.domain.common_function.formatTimeStampWithMonthName
import com.android.chatappcompose.core.domain.common_function.formatTimestampToAmPm
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.core.ui.commomComposables.ButtonsColorOfFullScreenMessageImageDialog
import com.android.chatappcompose.core.ui.commomComposables.CircularProgressIndicatorWithValue
import com.android.chatappcompose.core.ui.commomComposables.FullScreenImageDialog
import com.android.chatappcompose.core.ui.commomComposables.PickImageFromCameraAndGalleryDialog
import com.android.chatappcompose.main.chats.domain.model.ChatModel
import com.android.chatappcompose.main.chats.domain.model.MessageModel
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import com.android.chatappcompose.main.chats.ui.screens.MessageReactionBottomSheet
import com.android.chatappcompose.main.chats.ui.screens.MessageReactionDropdownMenu
import com.android.chatappcompose.main.chats.ui.screens.SingleChatScreenBottomBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@Composable
fun FullScreenMessageImageDialog(
    item: MessageModel?,
    messageBy: String,
    chatViewModel: ChatViewModel,
    chatId: String,
    chat: ChatModel?,
    currentUser: UserModel?,
    scale: Float,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {

    val focusRequester = remember { FocusRequester() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // For Message Reaction
    var isReactionMenuVisible by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val reactionMsgSheetState = rememberModalBottomSheetState()
    var msgReactionItem = mutableStateOf<MessageModel?>(null)

    // For Image Drag
    var backgroundAlpha by remember { mutableStateOf(0.3f) }
    var colorAlpha by remember { mutableStateOf(1f) }
    var imageOffsetY = remember { Animatable(0f) }
    val imageSize = with(LocalDensity.current) { 400.dp.toPx() }
    val maxDrag = imageSize / 2

    // For Message Reply
    val textFieldForReplySheetState = rememberModalBottomSheetState()
    var isSheetVisible by remember { mutableStateOf(false) }

    // For Check the Message Sended or Not
    var message by remember { mutableStateOf("") }

    var isMessageSended by remember { mutableStateOf(true) }
    var isImageMessageSended by remember { mutableStateOf(true) }
    var isCameraAndGalleryPickDialogOpen by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // For Image From Camera Or Gallery
    var isImageFromCameraOrGalleryClicked by remember { mutableStateOf(false) }
    var fullScreenImageFromCameraOrGalleyScale = remember { Animatable(0f) }

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

    if (isSheetVisible) {
        if (chatViewModel.repliedMsgItemId.value == null) {
            scope.launch {
                textFieldForReplySheetState.hide()
            }.invokeOnCompletion {
                if (!textFieldForReplySheetState.isVisible) {
                    isSheetVisible = false
                }
            }
        }
    }

    // For Image From Camera Or Gallery Clicked
    if (isImageFromCameraOrGalleryClicked) {
        scope.launch {
            fullScreenImageFromCameraOrGalleyScale.animateTo(1f)
        }
    } else {
        scope.launch {
            fullScreenImageFromCameraOrGalleyScale.animateTo(0f)
        }
    }


    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = if (textFieldForReplySheetState.isVisible) {
                false
            } else true
        ),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .background(color = Color.Black.copy(colorAlpha)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(alignment = Alignment.Center)
                        .onSizeChanged {
                            itemHeight = with(density) { it.height.toDp() }
                        }
                        .pointerInput(true) {
                            detectTapGestures(
                                onLongPress = {
                                    isReactionMenuVisible = true
                                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                }
                            )
                        }
                        .offset {
                            IntOffset(0, imageOffsetY.value.roundToInt())
                        }
                        .pointerInput(true) {
                            detectVerticalDragGestures(
                                onDragStart = { },
                                onDragEnd = {
                                    if (abs(imageOffsetY.value) < maxDrag) {
                                        scope.launch {
                                            imageOffsetY.animateTo(0f)
                                        }
                                        backgroundAlpha = 0.3f
                                        colorAlpha = 1f
                                    } else {
                                        onDismiss.invoke()
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    val newOffsetY =
                                        (imageOffsetY.value + dragAmount).coerceIn(
                                            -maxDrag,
                                            maxDrag
                                        )
                                    scope.launch {
                                        imageOffsetY.snapTo(newOffsetY)
                                    }
                                    // imageOffset = Offset(imageOffset.x, newOffsetY)
                                    backgroundAlpha = 1 - (abs(newOffsetY) / maxDrag)
                                    colorAlpha = 1 - (abs(newOffsetY) / maxDrag)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        modifier = Modifier,
                        model = item?.imageUrl ?: "",
                        contentDescription = "Chat Image",
                        contentScale = ContentScale.Fit,
                        loading = {
                            CircularProgressIndicator()
                        }
                    )

                    if (isReactionMenuVisible) {
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

                // Top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.DarkGray.copy(backgroundAlpha))
                        .align(alignment = Alignment.TopCenter),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onDismiss.invoke()
                            }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Full Image Back Icon",
                                tint = Color.White.copy(colorAlpha)
                            )
                        }

                        Column {
                            // message By Text
                            Text(
                                text = messageBy,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(colorAlpha)
                                )
                            )
                            // Message Time Text
                            Text(
                                text = "${
                                    formatTimeStampWithMonthName(
                                        item?.timeStamp,
                                        item?.monthName.toString()
                                    )
                                }, ${formatTimestampToAmPm(item?.timeStamp)}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = Color.White.copy(colorAlpha)
                                )
                            )
                        }
                    }
                }

                // Bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.DarkGray.copy(backgroundAlpha))
                        .align(alignment = Alignment.BottomCenter),
                ) {
                    if (!item?.message.isNullOrEmpty()) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 10.dp),
                            text = item?.message ?: "",
                            style = TextStyle(
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(colorAlpha)
                            ),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Message reactions Container

                            // if reaction exist
                            if (item?.reaction != null && item.reaction.size != 0) {
                                Button(
                                    colors = ButtonsColorOfFullScreenMessageImageDialog(colorAlpha),
                                    onClick = {
                                        msgReactionItem.value = item
                                        scope.launch {
                                            reactionMsgSheetState.show()
                                        }
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val reaction = item?.reaction?.last()

                                        Text(
                                            text = reaction?.reaction.toString(),
                                            style = TextStyle(
                                                fontSize = 15.sp
                                            )
                                        )

                                        item?.reaction?.size?.let {
                                            if (it > 1) {
                                                Text(
                                                    text = it.toString(),
                                                    style = TextStyle(
                                                        fontSize = 15.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // we have to show add reaction Icon even Reaction Exist or Not
                            Button(
                                colors = ButtonsColorOfFullScreenMessageImageDialog(colorAlpha),
                                onClick = {
                                    isReactionMenuVisible = true
                                    pressOffset = DpOffset.Zero
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddReaction,
                                    contentDescription = "Add Message Reaction Icon"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Message reply Container
                        Button(
                            colors = ButtonsColorOfFullScreenMessageImageDialog(colorAlpha),
                            onClick = {
                                chatViewModel.repliedMsgItemId.value = item?.key
                                scope.launch {
                                    isSheetVisible = true
                                    delay(500L)
                                    focusRequester.requestFocus()
                                }
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Reply,
                                    contentDescription = "Message Reply Icon"
                                )
                                Text(text = "Reply")
                            }
                        }
                    }

                }

                if (reactionMsgSheetState.isVisible) {
                    msgReactionItem.value?.let {
                        MessageReactionBottomSheet(
                            reactionMsgSheetState = reactionMsgSheetState,
                            messageModel = it,
                            chatViewModel = chatViewModel,
                            chatId = chatId,
                            scope = scope,
                            chat = chat,
                            currentUser = currentUser,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }

                if (isSheetVisible) {
                    FullScreenMessageImageBottomBarForMessageReply(
                        sheetState = textFieldForReplySheetState,
                        onDismiss = {
                            chatViewModel.repliedMsgItemId.value?.let {
                                chatViewModel.repliedMsgItemId.value = null
                            }
                            if (galleryImageBitmap.value != null || cameraImageBitmap.value != null) {
                                galleryImageUri.value = null
                                galleryImageBitmap.value = null
                                cameraImageBitmap.value = null
                            }

                            scope.launch {
                                textFieldForReplySheetState.hide()
                            }.invokeOnCompletion {
                                if (!textFieldForReplySheetState.isVisible) {
                                    isSheetVisible = false
                                }
                            }
                        },
                        content = {
                            SingleChatScreenBottomBar(
                                message = message,
                                onMessageChange = { message = it },
                                onMessageEmpty = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "message can't be empty",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                chatId = chatId,
                                chatViewModel = chatViewModel,
                                currentUser = currentUser,
                                galleryImageUri = galleryImageUri.value,
                                galleryImageBitmap = galleryImageBitmap.value,
                                cameraImageBitmap = cameraImageBitmap.value,
                                onCameraTrailingIconClicked = {
                                    isCameraAndGalleryPickDialogOpen = true
                                },
                                onImageCancel = {
                                    galleryImageUri.value = null
                                    galleryImageBitmap.value = null
                                    cameraImageBitmap.value = null
                                },
                                chat = chat,
                                replyMsgItem = item,
                                onReplyMsgContainerClicked = { },
                                focusRequester = focusRequester,
                                onTextMessageSuccess = {
                                    isMessageSended = true
                                    message = ""
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
                                            chatId = chatId,
                                            title = "Failed to Send Image",
                                            text = ""
                                        )
                                        galleryImageUri.value = null
                                        cameraImageBitmap.value = null
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
                                        chatId = chatId,
                                        messageKey = key,
                                        sended = true
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Photo Sent Successfully"
                                        )
                                    }
                                    chatViewModel.cancelProgressNotification()
                                    chatViewModel.notifySimpleNotificationForMessage(
                                        context = context,
                                        chatId = chatId,
                                        title = "Image Sent Successfully",
                                        text = ""
                                    )
                                    message = ""
                                    isImageMessageSended = true
                                    progress = 1f
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

                if (!isImageMessageSended) {

                    val animatedProgress = animateFloatAsState(
                        targetValue = progress,
                        animationSpec = if (!isImageMessageSended)
                            tween(durationMillis = 1000) else snap()
                    )

                    CircularProgressIndicatorWithValue(progress = animatedProgress.value)
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
    )
}

@Composable
fun FullScreenMessageImageBottomBarForMessageReply(
    sheetState: SheetState,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        containerColor = Color.Transparent,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.Transparent
            )
        },
        onDismissRequest = {
            onDismiss.invoke()
        },
        content = {
            content.invoke()
        }
    )
}