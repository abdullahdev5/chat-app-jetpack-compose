package com.android.chatappcompose.core.ui.commomComposables

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.chatappcompose.ui.theme.Green
import com.android.chatappcompose.ui.theme.TextFieldContainerColorLight

@Composable
fun TextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = if (isSystemInDarkTheme()) Color.DarkGray else TextFieldContainerColorLight,
    focusedIndicatorColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.Black,
    unfocusedIndicatorColor = Color.Transparent,
    focusedLabelColor = Green,
    unfocusedLabelColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    unfocusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    cursorColor = Green,
    focusedLeadingIconColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    unfocusedLeadingIconColor = if (isSystemInDarkTheme()) Color.White else Color.Black
)

@Composable
fun ButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Green,
    contentColor = Color.White
)

@Composable
fun ButtonsColorOfFullScreenMessageImageDialog(colorAlpha: Float) = ButtonDefaults.buttonColors(
    containerColor = Color.DarkGray.copy(colorAlpha),
    contentColor = Color.White.copy(colorAlpha),
)