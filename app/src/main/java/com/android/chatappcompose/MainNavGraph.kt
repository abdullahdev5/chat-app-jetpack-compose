package com.android.chatappcompose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.android.chatappcompose.main.MainScreen
import com.android.chatappcompose.main.chats.ui.ChatViewModel
import com.android.chatappcompose.main.chats.ui.screens.AddChatScreen
import com.android.chatappcompose.main.chats.ui.screens.MessageInfoScreen
import com.android.chatappcompose.main.chats.ui.screens.SingleChatScreen
import com.android.chatappcompose.main.chats.ui.screens.UpdateCHatScreen
import com.android.chatappcompose.notification.data.repository.MY_URI
import com.android.chatappcompose.profile.ui.screens.ProfileScreen
import com.android.chatappcompose.profile.ui.viewModel.ProfileViewModel
import com.android.chatappcompose.settings.chats.ui.screens.SettingsChatsScreen
import com.android.chatappcompose.settings.SettingsScreen
import com.android.chatappcompose.settings.chats.ui.screens.ChatsWallpaperScreen
import com.android.chatappcompose.settings.chats.ui.viewModel.SettingsChatsViewModel

@Composable
fun MainNavGraph(
    navHostController: NavHostController,
    chatViewModel: ChatViewModel,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit
) {

    NavHost(
        navController = navHostController,
        startDestination = SubGraph.MAIN_SUB,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
        },
//        popEnterTransition = {
//            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
//        },
//        popExitTransition = {
//            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
//        }
    ) {

        // Main Graph
        navigation<SubGraph.MAIN_SUB>(startDestination = MainDest.MAIN_SCREEN) {

            composable<MainDest.MAIN_SCREEN>() {
                MainScreen(
                    navHostController = navHostController,
                    chatViewModel = chatViewModel,
                    snackbarHostState = snackbarHostState,
                    onLogout = onLogout,
                )
            }

            composable<MainDest.ADD_CHAT_SCREEN>() {
                AddChatScreen(
                    navHostController = navHostController,
                    chatViewModel = chatViewModel
                )
            }

            composable<MainDest.UPDATE_CHAT_SCREEN>() {
                val arguments = it.toRoute<MainDest.UPDATE_CHAT_SCREEN>()
                UpdateCHatScreen(
                    navHostController = navHostController,
                    args = arguments,
                    chatViewModel = chatViewModel,
                    snackbarHostState = snackbarHostState
                )
            }

            composable<MainDest.SINGLE_CHAT_SCREEN>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "$MY_URI/chatId={chatId}"
                        chatViewModel.cancelSimpleNotification()
                    }
                )
            ) {
                val argument = it.toRoute<MainDest.SINGLE_CHAT_SCREEN>()
                SingleChatScreen(
                    navHostController = navHostController,
                    args = argument,
                    chatViewModel = chatViewModel,
                    snackbarHostState = snackbarHostState
                )
            }

            composable<MainDest.MESSAGE_INFO_SCREEN>() {
                val argument = it.toRoute<MainDest.MESSAGE_INFO_SCREEN>()
                MessageInfoScreen(
                    navHostController = navHostController,
                    args = argument,
                    chatViewModel = chatViewModel,
                    snackbarHostState = snackbarHostState
                )
            }

        }

        // Settings Graph
        navigation<SubGraph.SETTINGS_SUB>(startDestination = SettingsDest.SETTINGS_SCREEN) {

            composable<SettingsDest.SETTINGS_SCREEN>() {
                val profileViewModel = hiltViewModel<ProfileViewModel>()
                SettingsScreen(
                    navHostController = navHostController,
                    profileViewModel = profileViewModel
                )
            }

            composable<SettingsDest.PROFILE_SCREEN>(
                enterTransition = {
                    fadeIn()
                },
                exitTransition = {
                    fadeOut()
                },
                popEnterTransition = {
                    fadeIn()
                },
                popExitTransition = {
                    fadeOut()
                }
            ) {
                val profileViewModel = hiltViewModel<ProfileViewModel>()
                ProfileScreen(
                    navHostController = navHostController,
                    profileViewModel = profileViewModel,
                    snackbarHostState = snackbarHostState
                )
            }

            navigation<SubGraph.SETTINGS_CHATS_SUB>(startDestination = SettingsChatsSubDest.SETTINGS_CHATS_SCREEN) {

                composable<SettingsChatsSubDest.SETTINGS_CHATS_SCREEN>() {
                    val settingsChatsViewModel = hiltViewModel<SettingsChatsViewModel>()
                    SettingsChatsScreen(
                        navHostController = navHostController,
                        settingsChatsViewModel = settingsChatsViewModel
                    )
                }

                composable<SettingsChatsSubDest.CHATS_WAlLPAPER_SCREEN>() {
                    val settingsChatsViewModel = hiltViewModel<SettingsChatsViewModel>()
                    ChatsWallpaperScreen(
                        navHostController = navHostController,
                        settingsChatsViewModel = settingsChatsViewModel,
                        snackbarHostState = snackbarHostState
                    )
                }

            } // End of Settings Chat Sub Graph

        } // End of Settings Sub Graph

    }
}

