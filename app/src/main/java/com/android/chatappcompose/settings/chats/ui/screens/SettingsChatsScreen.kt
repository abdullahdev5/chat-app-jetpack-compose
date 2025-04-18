@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.settings.chats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android.chatappcompose.SettingsChatsSubDest
import com.android.chatappcompose.main.chats.domain.constants.FontSize
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.android.chatappcompose.settings.chats.ui.viewModel.SettingsChatsViewModel
import com.android.chatappcompose.ui.theme.Green

@Composable
fun SettingsChatsScreen(
    navHostController: NavHostController,
    settingsChatsViewModel: SettingsChatsViewModel
) {

    val scrollState = rememberScrollState()


    val chatsSettings = settingsChatsViewModel.chatsSettings.collectAsStateWithLifecycle()


    var isFontSizeDialogOpen = remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Chats")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Navigate Icon"
                        )
                    }
                }
            )
        }
    ) { paddingValue ->

        Column(
            modifier = Modifier
                .padding(paddingValue)
                .verticalScroll(scrollState)
        ) {

            // Display
            Text(
                modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp),
                text = "Display",
                color = Color.Gray
            )
            ChatsWallpaperContainer(
                navHostController = navHostController
            )

            Divider(modifier = Modifier.fillMaxWidth().padding(top = 20.dp))

            // Chats Settings
            Text(
                modifier = Modifier
                    .padding(start = 10.dp, top = 20.dp),
                text = "Chats Settings",
                color = Color.Gray
            )
            FontSizeContainer(
                onClick = {
                    isFontSizeDialogOpen.value = true
                },
                chatsSettings = chatsSettings.value
            )

        }

        if (isFontSizeDialogOpen.value) {
            FontSizeDialog(
                onClick = { fontSizeText ->
                    isFontSizeDialogOpen.value = false
                    settingsChatsViewModel.updateFontSize(fontSizeText)
                },
                chatsSettings = chatsSettings.value,
                onDismiss = {
                    isFontSizeDialogOpen.value = false
                }
            )
        }

    }

}

// Chats Wallpaper Related
@Composable
fun ChatsWallpaperContainer(
    navHostController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navHostController.navigate(SettingsChatsSubDest.CHATS_WAlLPAPER_SCREEN)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Wallpaper,
                contentDescription = null
            )

            Text(text = "Wallpaper")

        }
    }
}

// Font Size Related
@Composable
fun FontSizeContainer(
    onClick: () -> Unit,
    chatsSettings: SettingsChatsModel?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick.invoke()
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatSize,
                contentDescription = null,
                tint = Color.Transparent
            )

            Column {
                Text(text = "Font Size")
                Text(
                    text = chatsSettings?.fontSize ?: "",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }

        }
    }
}

@Composable
fun FontSizeDialog(
    onClick: (String) -> Unit,
    chatsSettings: SettingsChatsModel?,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        content = {
            val fontSizeList = listOf(
                "Small",
                "Medium",
                "Large",
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 20.dp),
                ) {

                    Text(
                        modifier = Modifier
                            .padding(all = 10.dp),
                        text = "Font Size",
                        style = TextStyle(
                            fontSize = FontSize.LARGE.sp,
                        )
                    )

                    var selectedItem = remember { mutableStateOf<String>(chatsSettings?.fontSize ?: "") }
                    LazyColumn(
                        modifier = Modifier
                        // .padding(all = 20.dp)
                    ) {
                        items(fontSizeList.size) { index ->

                            val item = fontSizeList[index]

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Green,
                                            unselectedColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                                        ),
                                        selected = selectedItem.value == item,
                                        onClick = {
                                            selectedItem.value = item
                                        }
                                    )
                                    Text(text = item)
                                }
                            }

                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            onDismiss.invoke()
                        }) {
                            Text(text = "Cancel")
                        }

                        TextButton(onClick = {
                            onClick.invoke(selectedItem.value)
                            onDismiss.invoke()
                        }) {
                            Text(text = "Ok")
                        }
                    }
                }
            }
        }
    )
}