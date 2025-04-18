package com.android.chatappcompose.core.ui.commomComposables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun CopyTextToClipBoard(
    textToCopy: String,
    context: Context,
    onCopy: () -> Unit,
) {
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)!!

    val clipData = ClipData.newPlainText("Copied Text", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
    onCopy.invoke()
}