@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.profile.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.core.ui.commomComposables.FullScreenImageDialog
import com.android.chatappcompose.core.ui.commomComposables.CircularProgressIndicatorWithValue
import com.android.chatappcompose.core.ui.commomComposables.PickImageFromCameraAndGalleryDialog
import com.android.chatappcompose.core.ui.commomComposables.TextFieldColors
import com.android.chatappcompose.profile.ui.viewModel.ProfileViewModel
import com.android.chatappcompose.ui.theme.Green
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    profileViewModel: ProfileViewModel,
    snackbarHostState: SnackbarHostState,
) {

    val context = LocalContext.current
    val editUsernameSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentUser = profileViewModel.currentUser.collectAsStateWithLifecycle()

    var isCameraAndGalleryPickDialogOpen by remember { mutableStateOf(false) }
    var isUserImageUpdated by remember { mutableStateOf(true) } // true By Default
    var isImageClicked by remember { mutableStateOf(false) }
    var animateImage by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val fullImageScale = remember {
        Animatable(0f)
    }


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

    LaunchedEffect(
        key1 = galleryImageBitmap.value,
        cameraImageBitmap.value
    ) {
        if (galleryImageBitmap.value != null || cameraImageBitmap.value != null) {
            scope.launch(Dispatchers.Main) {
                profileViewModel.updateUserProfilePic(
                    galleryImageBitmap.value?.let {
                        galleryImageBitmap.value
                    } ?: cameraImageBitmap.value!!
                ).collect { result ->
                    when (result) {
                        is ResultState.Failure -> {
                            isUserImageUpdated = true
                            progress = 0f
                            snackbarHostState.showSnackbar(
                                message = result.error.message.toString(),
                                duration = SnackbarDuration.Short
                            )
                        }

                        ResultState.Loading -> {
                            isUserImageUpdated = false
                        }

                        is ResultState.Progress -> {
                            progress = result.progress
                        }

                        is ResultState.Success -> {
                            progress = 1f
                            isUserImageUpdated = true
                            snackbarHostState.showSnackbar(
                                message = result.data,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = isImageClicked) {
        if (isImageClicked) {
            fullImageScale.animateTo(1f)
        } else {
            fullImageScale.animateTo(0f)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Profile")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
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
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                val imageSize by animateDpAsState(
                    targetValue = if (animateImage) 300.dp else 150.dp,
                    animationSpec = tween(durationMillis = 300)
                )

                if (!isImageClicked) {
                    Card(
                        modifier = Modifier
                            .width(imageSize)
                            .height(imageSize)
                            .clip(shape = RoundedCornerShape(55.dp))
                            .pointerInput(true) {
                                detectTapGestures(
                                    onTap = {
                                        scope.launch {
                                            animateImage = true
                                            delay(100)
                                            isImageClicked = true
                                        }
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.LightGray
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                        ) {
                            if (currentUser.value?.profilePic.isNullOrEmpty()) {
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
                                    model = currentUser.value?.profilePic ?: "",
                                    contentDescription = "User Profile Pic",
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

                    Card(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .clip(shape = RoundedCornerShape(25.dp))
                            .align(alignment = Alignment.BottomEnd)
                            .clickable {
                                isCameraAndGalleryPickDialogOpen = true
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Green,
                            contentColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Edit Profile Pic Icon"
                            )
                        }
                    }

                } else {
                    Spacer(modifier = Modifier.height(150.dp))
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
            // Username Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            editUsernameSheetState.show()
                        }
                    },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "username person Icon"
                    )

                    Column {
                        Text(
                            text = "Name",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color.Gray
                            ),
                        )

                        Text(
                            text = currentUser.value?.username ?: "",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit username Icon",
                        tint = Green
                    )

                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            // User Phone Number Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

                    },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "User Phone Number Icon"
                    )

                    Column {
                        Text(
                            text = "Phone Number",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color.Gray
                            ),
                        )

                        Text(
                            text = currentUser.value?.phoneNumber ?: "",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                }
            }

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

        if (isImageClicked) {
            FullScreenImageDialog(
                imageString = currentUser.value?.profilePic ?: "",
                imageBitmap = null,
                imageBy = "Profile Pic",
                scale = fullImageScale.value,
                onDismiss = {
                    scope.launch {
                        isImageClicked = false
                        delay(50)
                        animateImage = false
                    }
                }
            )
        }

        if (!isUserImageUpdated) {

            val animatedProgress = animateFloatAsState(
                targetValue = progress,
                animationSpec = if (!isUserImageUpdated)
                    tween(durationMillis = 1000) else snap()
            )

            CircularProgressIndicatorWithValue(progress = animatedProgress.value)
        }

        if (editUsernameSheetState.isVisible) {
            EditUsernameModalBottomSheet(
                editUsernameSheetState = editUsernameSheetState,
                onDismiss = {
                    scope.launch {
                        editUsernameSheetState.hide()
                    }
                },
                onSave = { username ->
                    if (username.isEmpty()) {
                        scope.launch {
                            editUsernameSheetState.hide()
                            snackbarHostState.showSnackbar(
                                message = "name can't be empty",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        profileViewModel.updateUsername(
                            username = username,
                            onSuccess = { successMsg ->
                                scope.launch {
                                    editUsernameSheetState.hide()
                                    snackbarHostState.showSnackbar(
                                        message = successMsg,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onFailure = { error ->
                                scope.launch {
                                    editUsernameSheetState.hide()
                                    snackbarHostState.showSnackbar(
                                        message = error,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                },
                currentUser = currentUser.value,
            )
        }

    }
}

@Composable
fun EditUsernameModalBottomSheet(
    editUsernameSheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    currentUser: UserModel?,
) {

    val focusRequester = remember { FocusRequester() }

    var username by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentUser?.username ?: "",
                selection = TextRange(0, currentUser?.username?.length ?: 0)
            )
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        sheetState = editUsernameSheetState,
        onDismissRequest = {
            onDismiss.invoke()
        },
        containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp)
            ) {
                Text(
                    text = "Enter Your Name",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    colors = TextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        TextButton(onClick = {
                            onDismiss.invoke()
                        }) {
                            Text(
                                text = "Cancel",
                                style = TextStyle(
                                    color = Green
                                )
                            )
                        }

                        TextButton(onClick = {
                            onSave.invoke(username.text)
                        }) {
                            Text(
                                text = "Save",
                                style = TextStyle(
                                    color = Green
                                )
                            )
                        }
                    }
                }

            }
        }
    )
}