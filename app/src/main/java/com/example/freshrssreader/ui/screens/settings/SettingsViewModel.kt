package com.example.freshrssreader.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshrssreader.data.repository.AuthRepository
import com.example.freshrssreader.data.repository.ArticleFilter
import com.example.freshrssreader.data.repository.BrowserMode
import com.example.freshrssreader.data.repository.SettingsRepository
import com.example.freshrssreader.data.repository.SortOrder
import com.example.freshrssreader.data.repository.ThemeMode
import com.example.freshrssreader.data.repository.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val viewMode: StateFlow<ViewMode> = settingsRepository.viewMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewMode.MEDIUM)

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val browserMode: StateFlow<BrowserMode> = settingsRepository.browserMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BrowserMode.INTERNAL)

    val sortOrder: StateFlow<SortOrder> = settingsRepository.sortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NEWEST_FIRST)

    val articleFilter: StateFlow<ArticleFilter> = settingsRepository.articleFilter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleFilter.UNREAD_ONLY)

    fun getServerUrl(): String {
        return runBlocking { authRepository.getServerUrl() ?: "" }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch { settingsRepository.setViewMode(mode) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setBrowserMode(mode: BrowserMode) {
        viewModelScope.launch { settingsRepository.setBrowserMode(mode) }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { settingsRepository.setSortOrder(order) }
    }

    fun setArticleFilter(filter: ArticleFilter) {
        viewModelScope.launch { settingsRepository.setArticleFilter(filter) }
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            val token = authRepository.getAuthToken() ?: ""
            authRepository.saveLoginData(url, "", token)
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
    }
}
