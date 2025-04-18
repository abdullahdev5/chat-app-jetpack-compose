package com.android.chatappcompose.core.ui.commomComposables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LoadingDialog() {
    Dialog(
        onDismissRequest = {}
    ) {
        CircularProgressIndicator(modifier = Modifier.width(40.dp))
    }
}

@Composable
fun CircularProgressIndicatorWithValue(progress: Float) {
    Dialog(
        onDismissRequest = {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = {
                    progress
                }
            )
        }
    }
}