package com.example.freshrssreader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshrssreader.data.repository.AuthRepository
import com.example.freshrssreader.data.repository.SettingsRepository
import com.example.freshrssreader.data.repository.ThemeMode
import com.example.freshrssreader.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            _startDestination.value = if (isLoggedIn) Routes.FEED_LIST else Routes.LOGIN
        }
    }
}
