package com.android.chatappcompose.core.ui.commomComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpView(
    otpValue: String,
    otpLength: Int = 6,
    onOtpChanged: (String) -> Unit
) {
    val focusRequesters= remember { List(otpLength) { FocusRequester() } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until otpLength) {
            OutlinedTextField(
                value = otpValue.getOrNull(i)?.toString() ?: "",
                onValueChange = { newValue ->
                    if (newValue.length <= 1) {
                        val updatedOtp = otpValue.take(i) + newValue + otpValue.drop(i + 1)
                        onOtpChanged(updatedOtp)

                        if (newValue.length < otpValue.getOrNull(i)?.toString()?.length ?: 0 && i > 0) {
                            // Move focus to the previous field when deleting a character
                            focusRequesters[i - 1].requestFocus()
                        } else if (i < otpLength - 1 && newValue.isNotEmpty()) {
                            // Move focus to the next field when entering a digit
                            focusRequesters[i + 1].requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .width(48.dp)
                    .padding(start = 5.dp)
                    .focusRequester(focusRequesters[i]),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}