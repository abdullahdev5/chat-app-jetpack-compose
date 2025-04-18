@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("SENSELESS_COMPARISON")

package com.android.chatappcompose.settings.chats.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.ui.commomComposables.CircularProgressIndicatorWithValue
import com.android.chatappcompose.core.ui.commomComposables.LoadingDialog
import com.android.chatappcompose.core.ui.commomComposables.PickImageFromCameraAndGalleryDialog
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.android.chatappcompose.settings.chats.ui.viewModel.SettingsChatsViewModel
import com.android.chatappcompose.settings.ui.screens.chats.defaultWallpapersUrl.DefaultChatWallpapersUrl
import com.android.chatappcompose.ui.theme.Green
import com.android.chatappcompose.ui.theme.LowGreenDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.let
import kotlin.text.isNullOrEmpty
import kotlin.toString


@Composable
fun ChatsWallpaperScreen(
    navHostController: NavHostController,
    settingsChatsViewModel: SettingsChatsViewModel,
    snackbarHostState: SnackbarHostState,
) {

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isImagePickerDialogOpen = remember { mutableStateOf(false) }
    var isWallpaperPreviewDismissDialogOpen = remember { mutableStateOf(false) }
    var isSetAsDefaultWallpaperDialogOpen = remember { mutableStateOf(false) }
    var isChatWallpaperUpdated = remember { mutableStateOf(true) } // True By Default
    var isWallpaperSetAsDefault = remember { mutableStateOf(true) } // True By Default
    var progress = remember { mutableStateOf(0f) }


    val chatsSettings by settingsChatsViewModel.chatsSettings.collectAsStateWithLifecycle()


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

    BackHandler {
        if (galleryImageUri.value != null || cameraImageBitmap.value != null) {
            isWallpaperPreviewDismissDialogOpen.value = true
        } else {
            navHostController.navigateUp()
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (isSystemInDarkTheme()) "Dark Theme Wallpaper" else "Light Theme Wallpaper")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (galleryImageUri.value != null || cameraImageBitmap.value != null) {
                            isWallpaperPreviewDismissDialogOpen.value = true
                        } else {
                            navHostController.navigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Navigate Icon"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            SingleChatScreenCopyForWallpaper(
                chatsSettings = chatsSettings,
                galleryImageBitmap = galleryImageBitmap.value,
                cameraImageBitmap = cameraImageBitmap.value,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Setting as Default Wallpaper button
                OutlinedButton(
                    onClick = {
                    isSetAsDefaultWallpaperDialogOpen.value = true
                }) {
                    Text(
                        text = "Set as Default",
                        color = if (isSystemInDarkTheme()) Color.White else Green
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Wallpaper Changing Button
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = Color.White
                    ),
                    onClick = {
                        if (galleryImageUri.value != null || cameraImageBitmap.value != null) {
                            scope.launch(Dispatchers.Main) {
                                settingsChatsViewModel.onChatWallpaperChanged(
                                    chatWallpaperBitmap = galleryImageBitmap.value?.let {
                                        galleryImageBitmap.value
                                    } ?: cameraImageBitmap.value!!
                                ).collect { result ->
                                    when (result) {
                                        is ResultState.Failure -> {
                                            isChatWallpaperUpdated.value = true
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = result.error.message.toString(),
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }

                                        ResultState.Loading -> {
                                            isChatWallpaperUpdated.value = false
                                        }

                                        is ResultState.Progress -> {
                                            progress.value = result.progress
                                        }

                                        is ResultState.Success -> {

                                            galleryImageUri.value = null
                                            galleryImageBitmap.value = null
                                            cameraImageBitmap.value = null

                                            progress.value = 1f
                                            isChatWallpaperUpdated.value = true
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = result.data,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            isImagePickerDialogOpen.value = true
                        }
                    }) {
                    Text(
                        text = if (galleryImageUri.value != null || cameraImageBitmap.value != null)
                            "Change" else "Preview"
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                text = if (isSystemInDarkTheme()) "This is the Wallpaper of Dark Theme." else "This is the Wallpaper of Light Theme.",
                style = TextStyle(
                    fontStyle = FontStyle.Italic
                )
            )

        }


        if (isImagePickerDialogOpen.value) {
            PickImageFromCameraAndGalleryDialog(
                onCameraClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onGalleryClick = {
                    galleyLauncher.launch("image/*")
                },
                onDismiss = {
                    isImagePickerDialogOpen.value = false
                }
            )
        }

        if (!isChatWallpaperUpdated.value) {
            val animatedProgress = animateFloatAsState(
                targetValue = progress.value,
                animationSpec = if (!isChatWallpaperUpdated.value)
                    tween(durationMillis = 1000) else snap()
            )

            CircularProgressIndicatorWithValue(progress = animatedProgress.value)
        }

        if (isWallpaperPreviewDismissDialogOpen.value) {
            WallpaperPreviewDismissDialog(
                onChange = {
                    isWallpaperPreviewDismissDialogOpen.value = false
                    scope.launch(Dispatchers.Main) {
                        settingsChatsViewModel.onChatWallpaperChanged(
                            chatWallpaperBitmap = galleryImageBitmap.value?.let {
                                galleryImageBitmap.value
                            } ?: cameraImageBitmap.value!!
                        ).collect { result ->
                            when (result) {
                                is ResultState.Failure -> {
                                    isChatWallpaperUpdated.value = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = result.error.message.toString(),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }

                                ResultState.Loading -> {
                                    isChatWallpaperUpdated.value = false
                                }

                                is ResultState.Progress -> {
                                    progress.value = result.progress
                                }

                                is ResultState.Success -> {

                                    galleryImageUri.value = null
                                    galleryImageBitmap.value = null
                                    cameraImageBitmap.value = null

                                    progress.value = 1f
                                    isChatWallpaperUpdated.value = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = result.data,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                onDiscard = {
                    galleryImageUri.value = null
                    galleryImageBitmap.value = null
                    cameraImageBitmap.value = null
                    isWallpaperPreviewDismissDialogOpen.value = false
                },
                onDismiss = {
                    isWallpaperPreviewDismissDialogOpen.value = false
                }
            )
        }

        if (isSetAsDefaultWallpaperDialogOpen.value) {
            SetAsDefaultWallpaperDialog(
                onSetAsDefault = {
                    isSetAsDefaultWallpaperDialogOpen.value = false
                    if (chatsSettings?.chatWallpaper.isNullOrEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "You are Already in Default Wallpaper",
                                duration = SnackbarDuration.Long
                            )
                        }
                    } else {
                        scope.launch(Dispatchers.Main) {
                            settingsChatsViewModel
                                .onChatWallpaperSetAsDefault()
                                .collect { result ->
                                    when (result) {
                                        is ResultState.Failure -> {
                                            isWallpaperSetAsDefault.value = true
                                            snackbarHostState.showSnackbar(
                                                message = result.error.message.toString(),
                                                duration = SnackbarDuration.Long
                                            )
                                        }

                                        ResultState.Loading -> {
                                            isWallpaperSetAsDefault.value = false
                                        }

                                        is ResultState.Progress -> TODO()
                                        is ResultState.Success -> {

                                            galleryImageUri.value = null
                                            galleryImageBitmap.value = null
                                            cameraImageBitmap.value = null

                                            isWallpaperSetAsDefault.value = true
                                            snackbarHostState.showSnackbar(
                                                message = result.data,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                        }
                    }
                },
                onCancel = {
                    isSetAsDefaultWallpaperDialogOpen.value = false
                }
            )
        }

        if (!isWallpaperSetAsDefault.value) {
            LoadingDialog()
        }


    }

}

@Composable
fun SingleChatScreenCopyForWallpaper(
    chatsSettings: SettingsChatsModel?,
    galleryImageBitmap: Bitmap?,
    cameraImageBitmap: Bitmap?,
) {
    // Single Chat Screen Copy For Wallpaper
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .padding(all = 55.dp)
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(10.dp))
                .background(color = if (isSystemInDarkTheme()) Color.Unspecified else Color.White)
        ) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // contact Image
                Card(
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                        .clip(shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(15.dp)
                                .height(15.dp),
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person Icon for Contact"
                        )
                    }
                }

                // Contact Name
                Text(text = "Contact Name", fontSize = 12.sp)
            }


            // Messages List Container
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min)
                    .clip(shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                    )),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Wallpaper Preview Or Actual User Chat Wallpaper
                    if (galleryImageBitmap != null || cameraImageBitmap != null) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            model = galleryImageBitmap?.let {
                                it
                            } ?: cameraImageBitmap,
                            contentDescription = "Preview Wallpaper Image",
                            contentScale = ContentScale.Crop
                        )

                    } else if (!chatsSettings?.chatWallpaper.isNullOrEmpty()) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            model = chatsSettings?.chatWallpaper ?: "",
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
                            .align(alignment = Alignment.TopCenter)
                    ) {
                        // Message Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 10.dp)
                                .padding(top = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                // Receiver Message
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .padding(end = 20.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .align(alignment = Alignment.Start),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
                                    )
                                ) {

                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                // Sender Message
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .padding(start = 20.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .align(alignment = Alignment.End),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSystemInDarkTheme()) LowGreenDark else Green,
                                    )
                                ) {

                                }
                            }
                        }
                    }

                    // Bottom Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 5.dp)
                            .align(alignment = Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(35.dp)
                                .clip(shape = RoundedCornerShape(15.dp))
                                .align(alignment = Alignment.CenterVertically),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(35.dp)
                                    .padding(all = 5.dp),
                            ) {
                                Text(
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterStart),
                                    text = "Message"
                                )

                                Icon(
                                    modifier = Modifier
                                        .align(alignment = Alignment.CenterEnd),
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Pick Image Icon"
                                )
                            }
                        }

                        IconButton(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(25.dp))
                                .background(color = Green)
                                .align(alignment = Alignment.CenterVertically),
                            enabled = false,
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                tint = Color.White,
                                contentDescription = "Send Message Icon"
                            )
                        }
                    }
                }
            }

        }
    } // End Of Single Chat Screen Copy For Wallpaper
}

@Composable
fun WallpaperPreviewDismissDialog(
    onChange: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
        onDismissRequest = {
            onDismiss.invoke()
        },
        text = {
            Text(text = "Wallpaper have not been changed.")
        },
        confirmButton = {
            TextButton(onClick = {
                onChange.invoke()
            }) {
                Text(text = "Change")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDiscard.invoke()
            }) {
                Text(text = "Discard")
            }
        }
    )
}

@Composable
fun SetAsDefaultWallpaperDialog(
    onSetAsDefault: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
        onDismissRequest = {
            onCancel.invoke()
        },
        text = {
            Text(text = "This will Remove Your Current Wallpaper.")
        },
        confirmButton = {
            TextButton(onClick = {
                onSetAsDefault.invoke()
            }) {
                Text(text = "Set as Default")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCancel.invoke()
            }) {
                Text(text = "Cancel")
            }
        }
    )
}