package com.android.chatappcompose.core.ui.commomComposables

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.chatappcompose.core.domain.common_function.formatTimestampToAmPm
import com.android.chatappcompose.main.chats.domain.model.ChatModel

@Composable
fun LastMessage(chat: ChatModel?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (chat?.lastMessageDeleted == true) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = chat?.lastMessage ?: "Message Deleted",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            if (!chat?.lastMessageImageUrl.isNullOrEmpty()) {
                Icon(
                    modifier = Modifier
                        .width(18.dp)
                        .height(18.dp)
                        .padding(end = 5.dp),
                    painter = painterResource(R.drawable.ic_menu_gallery),
                    contentDescription = "Image Pick from Gallery Icon"
                )
            }

            Text(
                modifier = Modifier
                    .weight(1f),
                text = chat?.lastMessage ?: "",
                style = TextStyle(
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

//        if (wantToShowTime == true) {
//            Spacer(modifier = Modifier.width(10.dp))
//            Text(
//                modifier = Modifier
//                    .padding(end = 20.dp),
//                text = chat?.lastMessageTime?.let {
//                    "${formatTimestampToAmPm(it)}"
//                } ?: "",
//                style = TextStyle(
//                    fontSize = 12.sp
//                )
//            )
//        }
    }
}