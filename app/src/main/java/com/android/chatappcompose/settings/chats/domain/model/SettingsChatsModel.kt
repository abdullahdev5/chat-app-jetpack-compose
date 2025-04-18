package com.android.chatappcompose.settings.chats.domain.model

data class SettingsChatsModel(
    val chatWallpaper: String? = "",
    val fontSize: String? = "", // Small, Medium, Large. Default is Medium
) {
    constructor(): this ("", "")
}
