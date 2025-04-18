package com.android.chatappcompose.core.ui.commomComposables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ErrorDialog(
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = "OK. I get it.")
            }
        }
    )
}