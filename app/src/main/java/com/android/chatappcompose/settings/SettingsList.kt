package com.android.chatappcompose.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.ui.graphics.vector.ImageVector

sealed class SettingsList(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val index: Int,
) {

    object Chats: SettingsList(
        icon = Icons.Outlined.Chat,
        title = "Chats",
        description = "wallpaper, font size, and more",
        index = 0,
    )

    object Privacy: SettingsList(
        icon = Icons.Outlined.Lock,
        title = "Privacy",
        description = "Password for Open App, and more",
        index = 1,
    )

}