package com.android.chatappcompose.settings.chats.ui.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.chatappcompose.auth.domain.resource.ResultState
import com.android.chatappcompose.settings.chats.domain.model.SettingsChatsModel
import com.android.chatappcompose.settings.chats.domain.repository.SettingsChatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsChatsViewModel @Inject constructor(
    private val settingsChatsRepo: SettingsChatsRepository
): ViewModel() {

    private val _chatsSettings = MutableStateFlow<SettingsChatsModel?>(null)
    val chatsSettings = _chatsSettings.asStateFlow()


    init {
        viewModelScope.launch {
            creatingSettingsChatsDocIfEmpty()
            setFontSizeToDefaultInInitOfViewModel()
            getChatsSettings()
        }
    }

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

    fun creatingSettingsChatsDocIfEmpty() = settingsChatsRepo.creatingSettingsChatsDocIfEmpty()

    fun onChatWallpaperChanged(chatWallpaperBitmap: Bitmap) =
        settingsChatsRepo.onChatWallpaperChanged(chatWallpaperBitmap)

    fun onChatWallpaperSetAsDefault() = settingsChatsRepo.onChatWallpaperSetAsDefault()

    fun setFontSizeToDefaultInInitOfViewModel() = settingsChatsRepo.setFontSizeToDefaultInInitOfViewModel()
    fun updateFontSize(fontSize: String) = settingsChatsRepo.updateFontSize(fontSize)


}