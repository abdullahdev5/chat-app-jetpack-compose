package com.android.chatappcompose.core.domain.model

import com.google.firebase.Timestamp

data class UserModel(
    val username: String = "",
    val phoneNumber: String = "",
    val profilePic: String? = null,
    val key: String = "",
    val timeStamp: Timestamp,
    val token: String? = ""
) {
    constructor(): this("", "", "", "", Timestamp.now(), "")
}