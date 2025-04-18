package com.android.chatappcompose.main.chats.ui

import android.R
import android.R.id.title
import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.constants.MESSAGES_COL
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.main.chats.domain.model.ChatModel
import com.android.chatappcompose.main.chats.domain.model.MessageModel
import com.android.chatappcompose.main.chats.domain.model.MessageReactionModel
import com.android.chatappcompose.main.chats.domain.repository.ChatRepository
import com.android.chatappcompose.notification.domain.repository.NotificationRepository
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.android.chatappcompose.settings.chats.domain.repository.SettingsChatsRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val settingsChatsRepo: SettingsChatsRepository,
    private val notificationRepo: NotificationRepository
) : ViewModel() {

    private var _chatsList = MutableStateFlow<List<ChatModel?>>(emptyList())
    var chatsList = _chatsList.asStateFlow()

    private val _chatById = MutableStateFlow<ChatModel?>(null)
    val chatById = _chatById.asStateFlow()

    private var _messagesList = MutableStateFlow<List<MessageModel?>>(emptyList())
    var messagesList = _messagesList.asStateFlow()

    private val _messageById = MutableStateFlow<MessageModel?>(null)
    val messageById = _messageById.asStateFlow()

    private val _repliedMessageByIdWhenDragComplete = MutableStateFlow<MessageModel?>(null)
    val repliedMessageByIdWhenDragComplete = _repliedMessageByIdWhenDragComplete.asStateFlow()

    var currentChatMessageListener: ListenerRegistration? = null

    val user: FirebaseUser?
        get() = chatRepo.currentUser

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser = _currentUser.asStateFlow()

    var totalUnreadCount = mutableStateOf(0)

    var selectedSenderItemsIds = mutableStateListOf<String?>()
    var selectedReceiverItemsIds = mutableStateListOf<String?>()
    var isMessageSelected = mutableStateOf(false)

    var msgReactionItem = mutableStateOf<MessageModel?>(null)
    var isMsgReactionAdded = mutableStateOf(true) // true By Default
    var isMsgReactionDeleted = mutableStateOf(true) // true By Default

    var repliedMsgItemId = mutableStateOf<String?>(null)

    var clickedRepliedMsgKey = mutableStateOf("")

    // Settings Related
    private val _chatsSettings = MutableStateFlow<SettingsChatsModel?>(null)
    val chatsSettings = _chatsSettings.asStateFlow()


    init {
        viewModelScope.launch {
            getChatsList()
            getUnreadCount()
        }
        // For CurrentUser
        viewModelScope.launch {
            getCurrentUser()
        }

        // For Chats Settings
        viewModelScope.launch {
            // Settings Related
            getChatsSettings()
        }
    }

    fun getChatsList() {
        chatRepo
            .chatsColRef
            ?.orderBy("lastMessageTime", Query.Direction.DESCENDING)
            ?.addSnapshotListener { value, error ->
                if (value != null) {
                    _chatsList.value = value.documents.mapNotNull {
                        it.toObject(ChatModel::class.java)
                    }
                }
            }
    }

    fun getChatByChatId(chatId: String) {
        chatRepo
            .chatsColRef
            ?.document(chatId)
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    _chatById.value = value.toObject(ChatModel::class.java)
                }
            }
    }

    suspend fun addChat(
        contactPhoneNumber: String,
        contactName: String,
    ) = chatRepo.addContact(contactPhoneNumber, contactName)

    fun updateChat(
        chatId: String,
        contactName: String,
    ) = chatRepo.updateChat(chatId, contactName)

    fun deleteChat(chatId: String) = chatRepo.deleteChat(chatId)

    fun onTextMessageSent(
        message: String,
        chatId: String,
        repliedMessage: String?,
        repliedMessageImageUrl: String?,
        repliedMessageKey: String?,
        senderRepliedMessageStatus: String?,
        receiverRepliedMessageStatus: String?,
    ) = chatRepo.onTextMessageSent(
        message = message,
        chatId = chatId,
        repliedMessage = repliedMessage,
        repliedMessageImageUrl = repliedMessageImageUrl,
        repliedMessageKey = repliedMessageKey,
        senderRepliedMessageStatus = senderRepliedMessageStatus,
        receiverRepliedMessageStatus = receiverRepliedMessageStatus,
    )

    suspend fun onImageMessageUpload(
        imageBitmap: Bitmap
    ) = chatRepo.onImageMessageUpload(
        imageBitmap = imageBitmap,
    )

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
    ) = chatRepo.onImageMessageSent(
        imageUri = imageUri,
        message = message,
        chatId = chatId,
        repliedMessage = repliedMessage,
        repliedMessageImageUrl = repliedMessageImageUrl,
        repliedMessageKey = repliedMessageKey,
        senderRepliedMessageStatus = senderRepliedMessageStatus,
        receiverRepliedMessageStatus = receiverRepliedMessageStatus,
        onImageMessageStores = onImageMessageStores
    )

    fun getMessages(chatId: String) {
        Log.d("TAG", "getMessages: chatId: $chatId")
        currentChatMessageListener = chatRepo
            .chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.orderBy("timeStamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    val messages = value.toObjects(MessageModel::class.java)
//                    val filteredMessages = messages.filter { message ->
//                        message.senderId == chatId || message.receiverId == chatId
//                    }
                    _messagesList.value = messages
                }
            }
    }

    fun depopulateMessages() {
        _messagesList.value = emptyList()
        currentChatMessageListener = null
    }

    fun deleteMsgForSender(chatId: String, keys: List<String>) =
        chatRepo.deleteMessageForSender(chatId, keys)

    fun deleteMsgForEveryone(chatId: String, keys: List<String>) =
        chatRepo.deleteMessageForEveryone(chatId, keys)

    fun getMessageById(chatId: String, messageKey: String) {
        chatRepo
            .chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document(messageKey)
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    _messageById.value = value.toObject(MessageModel::class.java)
                }
            }
    }

    fun getRepliedMsgByIdWhenDragComplete(chatId: String, messageKey: String) {
        chatRepo
            .chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document(messageKey)
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    _repliedMessageByIdWhenDragComplete.value = value.toObject(MessageModel::class.java)
                }
            }
    }

    suspend fun updateLastMessageOfChat(
        chatId: String,
        senderLastMsg: String?,
        receiverLastMsg: String?,
        lastMsgImageUrl: String?,
        timestamp: Timestamp?
    ) = chatRepo.updateLastMessageOfChat(chatId, senderLastMsg, receiverLastMsg, lastMsgImageUrl, timestamp)

    fun updateMessageSended(
        chatId: String,
        messageKey: String,
        sended: Boolean
    ) = chatRepo.updateMessageSended(chatId, messageKey, sended)

    suspend fun updateUnreadCount(chatId: String) {

        if (currentChatMessageListener != null) {
            chatRepo
                .chatsColRef
                ?.document(chatId)
                ?.update("unreadCount", 0)
        }

    }

    suspend fun getUnreadCount() {
        val documentSnapshot = chatRepo
            .chatsColRef
            ?.get()
            ?.await()

        for (document in documentSnapshot ?: emptyList()) {
            val unreadCount = document.getLong("unreadCount")?.toInt() ?: 0
            totalUnreadCount.value += unreadCount
        }
    }

    fun onMessageReactionAdd(
        chatId: String,
        messageKey: String,
        reaction: String
    ) = chatRepo.onMessageReactionAdd(chatId, messageKey, reaction)

    fun deleteMessageReaction(
        chatId: String,
        messageKey: String,
        reactionIndex: Int,
        reactionList: List<MessageReactionModel>
    ) = chatRepo.deleteMessageReaction(chatId, messageKey, reactionIndex, reactionList)

    suspend fun getCurrentUser() {
        chatRepo
            .getCurrentUser().collect { data ->
                when(data) {
                    is ResultState.Success<*> -> {
                        _currentUser.value = data.data as UserModel?
                    }
                    else -> {}
                }
            }
    }

    suspend fun updateChatUserFieldsInCurrentUser(
        chatId: String,
    ) = chatRepo.updateChatUserFieldsInCurrentUser(chatId)


    // SettingsRelated
    suspend fun getChatsSettings() {
        settingsChatsRepo.getChatsSettings().collect { data ->
            when(data) {
                is ResultState.Success<*> -> {
                    _chatsSettings.value = data.data as SettingsChatsModel?
                }
                else -> {}
            }
        }
    }

    fun onImageMessageSendNotificationWithProgress(
        context: Context,
        progress: Float,
    ) = notificationRepo.onImageMessageSendNotificationWithProgress(context, progress)

    fun notifySimpleNotificationForMessage(
        context: Context,
        chatId: String,
        title: String,
        text: String,
    ) = notificationRepo.notifySimpleNotificationForMessage(context, chatId, title, text)

    fun cancelProgressNotification() = notificationRepo.cancelProgressNotification()

    fun cancelSimpleNotification() = notificationRepo.cancelSimpleNotification()

}