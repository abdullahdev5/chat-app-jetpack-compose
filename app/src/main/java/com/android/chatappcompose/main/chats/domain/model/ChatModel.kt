package com.android.chatappcompose.main.chats.domain.model

import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp

@Stable
data class ChatModel(
    val username: String? = "",
    val usernameGivenByChatCreator: String? = "",
    val phoneNumber: String? = "",
    val profilePic: String? = "",
    val key: String? = "",
    val chatId: String? = "",
    var chatType: String? = "",
    var unreadCount: Int? = 0,
    var lastMessage: String? = "",
    var lastMessageImageUrl: String? = "",
    var lastMessageTime: Timestamp? = null,
    var lastMessageDeleted: Boolean,
    val timeStamp: Timestamp? = null
) {
    constructor(): this("", "", "", "", "", "", "", 0, "", "", null, false, null)
}
