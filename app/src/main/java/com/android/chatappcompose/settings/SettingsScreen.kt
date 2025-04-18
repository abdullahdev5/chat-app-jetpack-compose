@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.chatappcompose.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.android.chatappcompose.SettingsChatsSubDest
import com.android.chatappcompose.SettingsDest
import com.android.chatappcompose.profile.ui.viewModel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.isNullOrEmpty

@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    profileViewModel: ProfileViewModel
) {

    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val currentUser = profileViewModel.currentUser.collectAsStateWithLifecycle()

    val settingsList = listOf(
        SettingsList.Chats,
        SettingsList.Privacy,
    )

    var isUserProfileClicked by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(10.dp),
            state = lazyListState
        ) {

            item {
                Column(
                    modifier = Modifier
                        .padding(bottom = 30.dp)
                        .clickable {
                            scope.launch {
                                delay(200L)
                                isUserProfileClicked = true
                                delay(100L)
                                navHostController.navigate(SettingsDest.PROFILE_SCREEN)
                                delay(100L)
                                isUserProfileClicked = false
                            }
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, bottom = 10.dp),
                        horizontalArrangement = if (isUserProfileClicked) Arrangement.Center else Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val userProfilePicDp by animateDpAsState(
                            targetValue = if (isUserProfileClicked) 150.dp else 70.dp
                        )

                        Card(
                            modifier = Modifier
                                .width(userProfilePicDp)
                                .height(userProfilePicDp)
                                .clip(shape = RoundedCornerShape(50.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.LightGray
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                            ) {
                                if (currentUser.value?.profilePic.isNullOrEmpty()) {
                                    Icon(
                                        modifier = Modifier
                                            .align(alignment = Alignment.Center),
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                } else {
                                    SubcomposeAsyncImage(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        model = currentUser.value?.profilePic ?: "",
                                        contentDescription = "User Profile Pic",
                                        contentScale = ContentScale.FillWidth,
                                        loading = {
                                            Icon(
                                                modifier = Modifier
                                                    .align(alignment = Alignment.Center),
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Loading User Image",
                                                tint = Color.White,
                                            )
                                        },
                                        error = {
                                            Icon(
                                                modifier = Modifier
                                                    .align(alignment = Alignment.Center),
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Error User Image",
                                                tint = Color.White,
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = currentUser.value?.username ?: "",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                }
            }

            items(settingsList.size) { index ->

                val item = settingsList[index]

                SettingsList(
                    item = item,
                    navHostController = navHostController
                )

            }


        }
    }
}

@Composable
fun SettingsList(
    item: SettingsList,
    navHostController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.index == 0) {
                    navHostController.navigate(SettingsChatsSubDest.SETTINGS_CHATS_SCREEN)
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null
            )

            Column {
                Text(text = item.title)

                Text(
                    text = item.description,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }

        }
    }
}