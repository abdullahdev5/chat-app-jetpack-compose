package com.android.chatappcompose.main.chats.domain.model

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp


@Immutable
data class MessageModel(
    val senderId: String,
    val receiverId: String,
    val message: String? = "",
    val imageUrl: String? = "",
    val timeStamp: Timestamp,
    val monthName: String,
    var seen: Boolean,
    var sended: Boolean,
    var deleted: Boolean,
    val key: String,
    val reaction: List<MessageReactionModel>? = null,
    val repliedMessage: String? = "",
    val repliedMessageImageUrl: String? = "",
    val repliedMessageKey: String? = "",
    val repliedMessageStatus: String? = "",
) {
    constructor() : this("", "", "","", Timestamp.now(), "", false, false, false, "", null, "", "", "", "")
}
