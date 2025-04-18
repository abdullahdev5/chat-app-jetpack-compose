package com.android.chatappcompose.main.chats.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.core.domain.constants.CHATS_COL
import com.android.chatappcompose.core.domain.constants.MESSAGES_COL
import com.android.chatappcompose.core.domain.constants.MESSAGES_IMAGES_COL
import com.android.chatappcompose.core.domain.constants.USERS_COL
import com.android.chatappcompose.core.domain.model.UserModel
import com.android.chatappcompose.main.chats.domain.constants.INDIVIDUAL_CHAT_TYPE
import com.android.chatappcompose.main.chats.domain.model.ChatModel
import com.android.chatappcompose.main.chats.domain.model.MessageModel
import com.android.chatappcompose.main.chats.domain.model.MessageReactionModel
import com.android.chatappcompose.main.chats.domain.repository.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ChatRepository {

    private var _user = MutableStateFlow<UserModel?>(null)
    override var user = _user.asStateFlow()

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val userDocRef: DocumentReference?
        get() = fireStore.collection(USERS_COL).document(auth?.uid.toString())

    override val chatsColRef: CollectionReference?
        get() = userDocRef?.collection(CHATS_COL)

    override val usersCol: CollectionReference
        get() = fireStore.collection(USERS_COL)

    override val storageRef: StorageReference?
        get() = storage.reference



    override suspend fun addContact(
        contactPhoneNumber: String,
        contactName: String,
    ): Flow<ResultState<String>> = callbackFlow {

        trySend(ResultState.Loading)

        try {
            // Check if user already exists
            val existingUser = fireStore.collection(USERS_COL)
                .whereEqualTo("phoneNumber", contactPhoneNumber)
                .get()
                .await()
                .documents
                .firstOrNull()?.toObject(UserModel::class.java)

            if (existingUser == null) {
                trySend(ResultState.Failure(error("User not found")))
                return@callbackFlow
            }

            // Ensure not adding self as contact
            if (existingUser.key == _user.value?.key) {
                trySend(ResultState.Failure(error("Cannot add self as contact")))
                return@callbackFlow
            }

            val contact = existingUser.key?.let { existingUserKey ->
                chatsColRef
                    ?.document(existingUserKey)
                    ?.get()
                    ?.await()
            }

            if (contact?.exists()!!) {
                trySend(ResultState.Failure(error("Contact already exists")))
                return@callbackFlow
            } else {
                // Add user to current user's contacts

                val senderChatModel = existingUser.key?.let { existingUserKey ->
                    ChatModel(
                        existingUser.username ?: "",
                        contactName ?: "",
                        contactPhoneNumber ?: "",
                        existingUser.profilePic ?: "",
                        existingUserKey,
                        existingUserKey, // chatId
                        INDIVIDUAL_CHAT_TYPE,
                        null,
                        null,
                        null,
                        null,
                        false,
                        Timestamp.now()
                    )
                }

                senderChatModel?.let {
                    existingUser.key?.let { existingUserKey ->
                        chatsColRef
                            ?.document(existingUserKey)
                            ?.set(senderChatModel)
                            ?.addOnSuccessListener { snapshot ->

                                val receiverChatModel = ChatModel(
                                    _user.value?.username,
                                    "",
                                    _user.value?.phoneNumber ?: "",
                                    _user.value?.profilePic ?: "",
                                    _user.value?.key ?: "",
                                    _user.value?.key ?: "", // chatId
                                    INDIVIDUAL_CHAT_TYPE,
                                    0,
                                    null,
                                    null,
                                    null,
                                    false,
                                    Timestamp.now()
                                )
                                fireStore
                                    .collection(USERS_COL)
                                    .document(existingUserKey)
                                    .collection(CHATS_COL)
                                    .document(_user.value?.key.toString())
                                    .set(receiverChatModel)
                                    .addOnSuccessListener {
                                        trySend(ResultState.Success("Contact added successfully"))
                                    }
                            }
                            ?.addOnFailureListener { error ->
                                trySend(ResultState.Failure(error))
                            }
                    }
                }
            }

        } catch (e: Exception) {
            trySend(ResultState.Failure(e))
        }

        awaitClose {
            close()
        }
    }

    override fun updateChat(
        chatId: String, contactNameGivenByChatCreator: String
    ): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        val updateCurrentUserChatName = chatsColRef
            ?.document(chatId)

        updateCurrentUserChatName
            ?.update(
                "usernameGivenByChatCreator", contactNameGivenByChatCreator,
            )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(ResultState.Success("Contact Updated"))
                } else {
                    trySend(ResultState.Failure(task.exception!!))
                }
            }

        awaitClose {
            close()
        }
    }

    override fun deleteChat(chatId: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        // Deleting Another User Chat in Current User document
        val currentUserChat = chatsColRef
            ?.document(chatId)

        val anotherUserChat = fireStore
            .collection(USERS_COL)
            .document(chatId)
            .collection(CHATS_COL)
            .document(currentUser?.uid.toString())

        currentUserChat
            ?.collection(MESSAGES_COL)
            ?.get()
            ?.addOnSuccessListener {
                for (document in it) {
                    currentUserChat
                        .collection(MESSAGES_COL)
                        .document(document.id)
                        .delete()
                }
            }
            ?.addOnSuccessListener {
                currentUserChat
                    ?.delete()
            }

        anotherUserChat
            .collection(MESSAGES_COL)
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    anotherUserChat
                        .collection(MESSAGES_COL)
                        .document(document.id)
                        .delete()
                }
            }
            .addOnSuccessListener {
                anotherUserChat
                    .delete()
                    .addOnSuccessListener {
                        trySend(ResultState.Success("Chat Deleted"))
                    }
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun onTextMessageSent(
        message: String,
        chatId: String,
        repliedMessage: String?,
        repliedMessageImageUrl: String?,
        repliedMessageKey: String?,
        senderRepliedMessageStatus: String?,
        receiverRepliedMessageStatus: String?,
    ): Flow<ResultState<MessageModel>> =
        callbackFlow {
            trySend(ResultState.Loading)

            val currentDate = LocalDate.now()
            val key = chatsColRef
                ?.document(chatId)
                ?.collection(MESSAGES_COL)
                ?.document()
                ?.id
                .toString()

            val senderMessageModel = MessageModel(
                senderId = currentUser?.uid.toString(),
                receiverId = chatId,
                message = message,
                imageUrl = "",
                timeStamp = Timestamp.now(),
                currentDate.month.name,
                true, // isSeen
                false, // isSended
                false, // isDeleted
                key = key,
                null, // reaction
                if (repliedMessage.isNullOrEmpty()) null else repliedMessage, // replied Message
                if (repliedMessageImageUrl.isNullOrEmpty()) null else repliedMessageImageUrl, // replied Message
                if (repliedMessageKey.isNullOrEmpty()) null else repliedMessageKey, // replied Message Key
                if (senderRepliedMessageStatus.isNullOrEmpty()) null else senderRepliedMessageStatus, // replied Message Status
            )

            val receiverMessageModel = MessageModel(
                senderId = currentUser?.uid.toString(),
                receiverId = chatId,
                message = message,
                imageUrl = "",
                timeStamp = Timestamp.now(),
                currentDate.month.name,
                true, // isSeen
                false, // isSended
                false, // isDeleted
                key = key,
                null, // reaction
                if (repliedMessage.isNullOrEmpty()) null else repliedMessage, // replied Message
                if (repliedMessageImageUrl.isNullOrEmpty()) null else repliedMessageImageUrl, // replied Message
                if (repliedMessageKey.isNullOrEmpty()) null else repliedMessageKey, // replied Message Key
                if (receiverRepliedMessageStatus.isNullOrEmpty()) null else receiverRepliedMessageStatus, // replied Message Status
            )

            chatsColRef
                ?.document(chatId)
                ?.collection(MESSAGES_COL)
                ?.document(key)
                ?.set(senderMessageModel)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        fireStore
                            .collection(USERS_COL)
                            .document(chatId)
                            .collection(CHATS_COL)
                            .document(currentUser?.uid.toString())
                            .collection(MESSAGES_COL)
                            .document(key)
                            .set(receiverMessageModel)
                            .addOnSuccessListener {
                                trySend(ResultState.Success(senderMessageModel))
                            }
                            .addOnFailureListener {
                                trySend(ResultState.Failure(it))
                            }
                    } else {
                        trySend(ResultState.Failure(task.exception!!))
                    }
                }

            // increment unreadCount
            fireStore
                .collection(USERS_COL)
                .document(chatId)
                .collection(CHATS_COL)
                .document(currentUser?.uid.toString())
                .update("unreadCount", FieldValue.increment(1))

            awaitClose {
                close()
            }
        }

    override suspend fun onImageMessageUpload(
        imageBitmap: Bitmap
    ): Flow<ResultState<Uri>> = callbackFlow {
        trySend(ResultState.Loading)

        val imageRef =
            storageRef?.child(MESSAGES_IMAGES_COL)?.child(" ${UUID.randomUUID()} ${imageBitmap}")

        // baos means ByteArrayOutputStream
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInByte = baos.toByteArray()

        val uploadTask = imageRef?.putBytes(imageInByte)

        uploadTask
            ?.addOnSuccessListener { document ->
                val url = document.storage.downloadUrl

                url.addOnCompleteListener { uri ->
                    trySend(ResultState.Success(uri.result))
                }
            }
            ?.addOnFailureListener { error ->
                trySend(ResultState.Failure(error))
            }
            ?.addOnProgressListener { snapshot ->
                val progress = 1.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                trySend(ResultState.Progress(progress.toFloat()))
            }

        awaitClose {
            close()
        }
    }

    override fun onImageMessageSent(
        imageUrl: Uri,
        message: String,
        chatId: String,
        repliedMessage: String?,
        repliedMessageImageUrl: String?,
        repliedMessageKey: String?,
        senderRepliedMessageStatus: String?,
        receiverRepliedMessageStatus: String?,
        onImageMessageStores: (String) -> Unit
    ) {

        val currentDate = LocalDate.now()
        val key = chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document()
            ?.id
            .toString()

        val senderMessageModel = MessageModel(
            senderId = currentUser?.uid.toString(),
            receiverId = chatId,
            message = message,
            imageUrl = imageUrl.toString(),
            timeStamp = Timestamp.now(),
            currentDate.month.name,
            true, // isSeen
            false, // isSended
            false, // isDeleted
            key = key,
            null, // reaction
            if (repliedMessage.isNullOrEmpty()) null else repliedMessage, // replied Message
            if (repliedMessageImageUrl.isNullOrEmpty()) null else repliedMessageImageUrl, // replied Message
            if (repliedMessageKey.isNullOrEmpty()) null else repliedMessageKey, // replied Message Key
            if (senderRepliedMessageStatus.isNullOrEmpty()) null else senderRepliedMessageStatus, // replied Message Status,
        )

        val receiverMessageModel = MessageModel(
            senderId = currentUser?.uid.toString(),
            receiverId = chatId,
            message = message,
            imageUrl = imageUrl.toString(),
            timeStamp = Timestamp.now(),
            currentDate.month.name,
            true, // isSeen
            false, // isSended
            false, // isDeleted
            key = key,
            null, // reaction
            if (repliedMessage.isNullOrEmpty()) null else repliedMessage, // replied Message
            if (repliedMessageImageUrl.isNullOrEmpty()) null else repliedMessageImageUrl, // replied Message
            if (repliedMessageKey.isNullOrEmpty()) null else repliedMessageKey, // replied Message Key
            if (receiverRepliedMessageStatus.isNullOrEmpty()) null else receiverRepliedMessageStatus, // replied Message Status,
        )

        chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document(key)
            ?.set(senderMessageModel)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fireStore
                        .collection(USERS_COL)
                        .document(chatId)
                        .collection(CHATS_COL)
                        .document(currentUser?.uid.toString())
                        .collection(MESSAGES_COL)
                        .document(key)
                        .set(receiverMessageModel)
                        .addOnSuccessListener {
                            onImageMessageStores.invoke(key)
                        }
                        .addOnFailureListener {

                        }
                } else {
                    return@addOnCompleteListener
                }
            }

        // increment unreadCount
        fireStore
            .collection(USERS_COL)
            .document(chatId)
            .collection(CHATS_COL)
            .document(currentUser?.uid.toString())
            .update("unreadCount", FieldValue.increment(1))

    }

    override fun deleteMessageForSender(
        chatId: String,
        keys: List<String>
    ): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        try {
            for (key in keys) {
                val deleteSenderMsg = chatsColRef
                    ?.document(chatId)
                    ?.collection(MESSAGES_COL)
                    ?.document(key)

                deleteSenderMsg
                    ?.update(
                        "deleted", true,
                        "message", "Deleted Message",
                        "imageUrl", null,
                        "reaction", null,
                        "repliedMessage", null,
                        "repliedMessageImageUrl", null,
                        "repliedMessageKey", null,
                        "repliedMessageStatus", null
                    )
                    ?.addOnSuccessListener {
                        trySend(ResultState.Success("Message Deleted"))
                    }
                    ?.addOnFailureListener {
                        trySend(ResultState.Failure(it))
                    }
            }
        } catch (e: Exception) {
            trySend(ResultState.Failure(e))
        }
        awaitClose {
            close()
        }
    }

    override fun deleteMessageForEveryone(
        chatId: String,
        keys: List<String>
    ): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        try {
            for (key in keys) {
                val deleteSenderMsg = chatsColRef
                    ?.document(chatId)
                    ?.collection(MESSAGES_COL)
                    ?.document(key)

                deleteSenderMsg
                    ?.update(
                        "deleted", true,
                        "message", "Deleted Message",
                        "imageUrl", null,
                        "reaction", null,
                        "repliedMessage", null,
                        "repliedMessageImageUrl", null,
                        "repliedMessageKey", null,
                        "repliedMessageStatus", null
                    )
                    ?.addOnSuccessListener {
                        val deleteReceiverMsg = fireStore
                            .collection(USERS_COL)
                            .document(chatId)
                            .collection(CHATS_COL)
                            .document(currentUser?.uid.toString())
                            .collection(MESSAGES_COL)
                            .document(key)

                        deleteReceiverMsg
                            .update(
                                "deleted", true,
                                "message", "Deleted Message",
                                "imageUrl", null,
                                "reaction", null,
                                "repliedMessage", null,
                                "repliedMessageImageUrl", null,
                                "repliedMessageKey", null,
                                "repliedMessageStatus", null
                            )
                            .addOnSuccessListener {
                                trySend(ResultState.Success("Message Deleted"))
                            }

                    }
                    ?.addOnFailureListener {
                        trySend(ResultState.Failure(it))
                    }

            }
        } catch (e: Exception) {
            trySend(ResultState.Failure(e))
        }
        awaitClose {
            close()
        }
    }

    override suspend fun updateLastMessageOfChat(
        chatId: String,
        senderLastMsg: String?,
        receiverLastMsg: String?,
        lastMsgImageUrl: String?,
        timestamp: Timestamp?
    ) {

        val lastMsgByTime = chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.orderBy("timeStamp", Query.Direction.DESCENDING)
            ?.get()
            ?.await()
            ?.documents
            ?.firstOrNull()?.toObject(MessageModel::class.java)

        // Update User chats Fields Like last Message or lastMessageTime
        chatsColRef
            ?.document(chatId)
            ?.update(
                "lastMessage",
                if (senderLastMsg == null)
                    lastMsgByTime?.message
                else senderLastMsg,
                "lastMessageImageUrl", lastMsgImageUrl,
                "lastMessageTime",
                if (timestamp == null)
                    lastMsgByTime?.timeStamp
                else timestamp,
            )
            ?.addOnSuccessListener {
                fireStore
                    .collection(USERS_COL)
                    .document(chatId)
                    .collection(CHATS_COL)
                    .document(currentUser?.uid.toString())
                    .update(
                        "lastMessage",
                        if (receiverLastMsg == null)
                            lastMsgByTime?.message
                        else receiverLastMsg,
                        "lastMessageImageUrl", lastMsgImageUrl,
                        "lastMessageTime",
                        if (timestamp == null)
                            lastMsgByTime?.timeStamp
                        else timestamp,
                    )
                    .addOnSuccessListener {

                    }
            }
            ?.addOnFailureListener {
                return@addOnFailureListener
            }
    }

    override fun updateMessageSended(chatId: String, messageKey: String, sended: Boolean) {
        chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document(messageKey)
            ?.update("sended", sended)
            ?.addOnSuccessListener {

            }
            ?.addOnFailureListener {
                return@addOnFailureListener
            }
    }

    override fun onMessageReactionAdd(
        chatId: String,
        messageKey: String,
        reaction: String
    ): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        val key = UUID.randomUUID()

        val reactionModel = MessageReactionModel(
            reaction = reaction,
            reactionAddedBy = currentUser?.uid,
            key = key.toString(),
            timeStamp = Timestamp.now()
        )

        chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document(messageKey)
            ?.update("reaction", FieldValue.arrayUnion(reactionModel))
            ?.addOnSuccessListener {

                usersCol
                    .document(chatId)
                    .collection(CHATS_COL)
                    .document(currentUser?.uid.toString())
                    .collection(MESSAGES_COL)
                    .document(messageKey)
                    .update("reaction", FieldValue.arrayUnion(reactionModel))
                    .addOnSuccessListener {
                        trySend(ResultState.Success("Your Reaction is Added"))
                    }
            }
            ?.addOnFailureListener {
                trySend(ResultState.Failure(it))
            }


        awaitClose {
            close()
        }
    }

    override fun deleteMessageReaction(
        chatId: String,
        messageKey: String,
        reactionIndex: Int,
        reactionList: List<MessageReactionModel>
    ): Flow<ResultState<String>> = callbackFlow {

        trySend(ResultState.Loading)

        val updatedList = reactionList.toMutableList().also {
            it.removeAt(reactionIndex)
        }


        chatsColRef
            ?.document(chatId)
            ?.collection(MESSAGES_COL)
            ?.document(messageKey)
            ?.update("reaction", updatedList)
            ?.addOnSuccessListener {

                usersCol
                    .document(chatId)
                    .collection(CHATS_COL)
                    .document(currentUser?.uid.toString())
                    .collection(MESSAGES_COL)
                    .document(messageKey)
                    .update("reaction", updatedList)
                    .addOnSuccessListener {
                        trySend(ResultState.Success("Your Reaction is Removed"))
                    }
            }
            ?.addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun getCurrentUser(): Flow<ResultState<UserModel?>> = callbackFlow {
        fireStore
            .collection(USERS_COL)
            .document(auth.currentUser?.uid.toString())
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }


                if (value != null) {
                    val user = value.toObject(UserModel::class.java)
                    trySend(ResultState.Success(user))
                }
            }

        awaitClose {
            close()
        }
    }

    override suspend fun updateChatUserFieldsInCurrentUser(chatId: String) {

        val chatUserByChatId = usersCol
            .document(chatId)
            .get()
            .await()
            .toObject(UserModel::class.java)

        chatsColRef
            ?.document(chatId)
            ?.update(
                "username", chatUserByChatId?.username ?: "",
                "profilePic", chatUserByChatId?.profilePic ?: "",
            )
            ?.addOnSuccessListener {

            }
            ?.addOnFailureListener {
                return@addOnFailureListener
            }
    }

}