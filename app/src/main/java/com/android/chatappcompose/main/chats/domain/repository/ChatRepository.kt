package com.android.chatappcompose.main.chats.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.main.chats.domain.model.MessageModel
import com.android.chatappcompose.main.chats.domain.model.MessageReactionModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {

    var user: StateFlow<UserModel?>

    val userDocRef: DocumentReference?

    val chatsColRef: CollectionReference?

    val currentUser: FirebaseUser?

    val usersCol: CollectionReference

    val storageRef: StorageReference?

    suspend fun addContact(
        contactPhoneNumber: String,
        contactName: String,
    ): Flow<ResultState<String>>

    fun updateChat(chatId: String, contactNameGivenByChatCreator: String)
    : Flow<ResultState<String>>

    fun deleteChat(chatId: String): Flow<ResultState<String>>

    fun onTextMessageSent(
        message: String,
        chatId: String,
        repliedMessage: String?,
        repliedMessageImageUrl: String?,
        repliedMessageKey: String?,
        senderRepliedMessageStatus: String?,
        receiverRepliedMessageStatus: String?,
    ): Flow<ResultState<MessageModel>>

    suspend fun onImageMessageUpload(
        imageBitmap: Bitmap
    ): Flow<ResultState<Uri>>

    fun onImageMessageSent(
        imageUri: Uri,
        message: String,
        chatId: String,
        repliedMessage: String?,
        repliedMessageImageUrl: String?,
        repliedMessageKey: String?,
        senderRepliedMessageStatus: String?,
        receiverRepliedMessageStatus: String?,
        onImageMessageStores: (String) -> Unit
    )

    fun deleteMessageForSender(chatId: String, keys: List<String>): Flow<ResultState<String>>

    fun deleteMessageForEveryone(chatId: String, keys: List<String>): Flow<ResultState<String>>

    suspend fun updateLastMessageOfChat(
        chatId: String,
        senderLastMsg: String?,
        receiverLastMsg: String?,
        lastMsgImageUrl: String?,
        timestamp: Timestamp?
    )

    fun updateMessageSended(chatId: String, messageKey: String, sended: Boolean)

    fun onMessageReactionAdd(chatId: String, messageKey: String, reaction: String): Flow<ResultState<String>>

    fun deleteMessageReaction(
        chatId: String, messageKey: String, reactionIndex: Int, reactionList: List<MessageReactionModel>
    ): Flow<ResultState<String>>

    fun getCurrentUser(): Flow<ResultState<UserModel?>>

    suspend fun updateChatUserFieldsInCurrentUser(chatId: String)

}