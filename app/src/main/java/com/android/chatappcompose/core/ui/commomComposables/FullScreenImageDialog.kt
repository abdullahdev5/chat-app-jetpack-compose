package com.android.chatappcompose.core.ui.commomComposables

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FullScreenImageDialog(
    imageString: String,
    imageBitmap: Bitmap?,
    imageBy: String,
    scale: Float,
    onDismiss: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var backgroundAlpha by remember { mutableStateOf(1f) }
    var topBarColorAlpha by remember { mutableStateOf(1f) }

    var imageOffsetY = remember { Animatable(0f) }
    val imageSize = with(LocalDensity.current) { 400.dp.toPx() }

    val maxDrag = imageSize / 2

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .background(color = Color.Black.copy(backgroundAlpha)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(alignment = Alignment.Center)
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
                                        backgroundAlpha = 1f
                                        topBarColorAlpha = 1f
                                    } else {
                                        onDismiss.invoke()
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    val newOffsetY =
                                        (imageOffsetY.value + dragAmount).coerceIn(-maxDrag, maxDrag)
                                    scope.launch {
                                        imageOffsetY.snapTo(newOffsetY)
                                    }
                                    // imageOffset = Offset(imageOffset.x, newOffsetY)
                                    backgroundAlpha = 1 - (abs(newOffsetY) / maxDrag)
                                    topBarColorAlpha = 1 - (abs(newOffsetY) / maxDrag)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageString.isEmpty() && imageBitmap == null) {
                        Text(
                            text = "No Profile Pic",
                            style = TextStyle(
                                color = Color.White,
                            )
                        )
                    } else {
                        SubcomposeAsyncImage(
                            model = if (imageString.isEmpty())
                                imageBitmap else imageString,
                            contentDescription = "Chat Image",
                            contentScale = ContentScale.Fit,
                            loading = {
                                CircularProgressIndicator()
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.TopCenter),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            onDismiss.invoke()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Full Image Back Icon",
                                tint = Color.White.copy(topBarColorAlpha)
                            )
                        }

                        Text(
                            text = imageBy,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(topBarColorAlpha)
                            )
                        )
                    }
                }

            }
        }
    )
}