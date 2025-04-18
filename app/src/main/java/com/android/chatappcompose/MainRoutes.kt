package com.android.chatappcompose

import kotlinx.serialization.Serializable

sealed class SubGraph {

    @Serializable
    object MAIN_SUB : SubGraph()

    @Serializable
    object SETTINGS_SUB : SubGraph()

    @Serializable
    object SETTINGS_CHATS_SUB : SubGraph()

}

sealed class MainDest {
    @Serializable
    object MAIN_SCREEN : MainDest()

    @Serializable
    object ADD_CHAT_SCREEN : MainDest()

    @Serializable
    data class UPDATE_CHAT_SCREEN(
        val chatId: String,
        val username: String,
        val usernameGivenByChatCreator: String? = ""

    ) : MainDest()

    @Serializable
    data class SINGLE_CHAT_SCREEN(
        val chatId: String

    ) : MainDest()

    @Serializable
    data class MESSAGE_INFO_SCREEN(
        val chatId: String,
        val messageKey: String

    ) : MainDest()

}

// Settings Destination
sealed class SettingsDest {

    @Serializable
    object SETTINGS_SCREEN : SettingsDest()

    @Serializable
    object PROFILE_SCREEN : SettingsDest()

}

// Settings Chats Sub Destination
sealed class SettingsChatsSubDest {

    @Serializable
    object SETTINGS_CHATS_SCREEN : SettingsChatsSubDest()

    @Serializable
    object CHATS_WAlLPAPER_SCREEN : SettingsChatsSubDest()

}