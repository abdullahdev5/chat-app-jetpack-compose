package com.android.chatappcompose.main.chats.domain.model

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp

@Immutable
data class MessageReactionModel(
    val reaction: String? = "",
    val reactionAddedBy: String? = "",
    val key: String? = "",
    val timeStamp: Timestamp? = null,
) {
    constructor() : this ("" ,"", "", null)
}
