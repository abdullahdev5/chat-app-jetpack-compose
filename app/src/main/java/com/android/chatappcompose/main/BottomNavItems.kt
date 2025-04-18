package com.android.chatappcompose.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Update
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItems(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean? = null,
    val badgeCount: Int? = null
) {
    object Chats : BottomNavItems(
        title = "Chats",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat,
        hasNews = false,
        badgeCount = 0
    )

    object Updates : BottomNavItems(
        title = "Updates",
        selectedIcon = Icons.Filled.Update,
        unselectedIcon = Icons.Outlined.Update,
        hasNews = false,
        badgeCount = 0
    )


}