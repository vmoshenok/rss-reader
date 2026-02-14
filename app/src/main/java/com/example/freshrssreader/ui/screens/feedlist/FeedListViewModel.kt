package com.example.freshrssreader.ui.screens.feedlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshrssreader.data.model.Category
import com.example.freshrssreader.data.model.Feed
import com.example.freshrssreader.data.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedListUiState(
    val feeds: List<Feed> = emptyList(),
    val categories: List<Category> = emptyList(),
    val feedsByCategory: Map<Category, List<Feed>> = emptyMap(),
    val uncategorizedFeeds: List<Feed> = emptyList(),
    val totalUnread: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedListViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedListUiState())
    val uiState: StateFlow<FeedListUiState> = _uiState.asStateFlow()

    init {
        loadFeeds()
    }

    fun loadFeeds() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            feedRepository.getSubscriptions()
                .onSuccess { feeds ->
                    val allCategories = feeds.flatMap { it.categories }.distinctBy { it.id }
                    val feedsByCategory = allCategories.associateWith { category ->
                        feeds.filter { feed ->
                            feed.categories.any { it.id == category.id }
                        }
                    }
                    val uncategorized = feeds.filter { it.categories.isEmpty() }
                    val totalUnread = feeds.sumOf { it.unreadCount }

                    _uiState.value = FeedListUiState(
                        feeds = feeds,
                        categories = allCategories,
                        feedsByCategory = feedsByCategory,
                        uncategorizedFeeds = uncategorized,
                        totalUnread = totalUnread,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load feeds"
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            feedRepository.getSubscriptions()
                .onSuccess { feeds ->
                    val allCategories = feeds.flatMap { it.categories }.distinctBy { it.id }
                    val feedsByCategory = allCategories.associateWith { category ->
                        feeds.filter { feed ->
                            feed.categories.any { it.id == category.id }
                        }
                    }
                    val uncategorized = feeds.filter { it.categories.isEmpty() }
                    val totalUnread = feeds.sumOf { it.unreadCount }

                    _uiState.value = FeedListUiState(
                        feeds = feeds,
                        categories = allCategories,
                        feedsByCategory = feedsByCategory,
                        uncategorizedFeeds = uncategorized,
                        totalUnread = totalUnread,
                        isRefreshing = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = e.localizedMessage
                    )
                }
        }
    }
}
