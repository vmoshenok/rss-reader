package com.example.freshrssreader.ui.screens.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshrssreader.data.model.Article
import com.example.freshrssreader.data.repository.ArticleFilter
import com.example.freshrssreader.data.repository.FeedRepository
import com.example.freshrssreader.data.repository.SettingsRepository
import com.example.freshrssreader.data.repository.SortOrder
import com.example.freshrssreader.data.repository.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleListUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val continuation: String? = null,
    val error: String? = null
)

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val sharedArticleHolder: SharedArticleHolder,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Raw articles from API (unfiltered, unsorted)
    private val _rawArticles = MutableStateFlow<List<Article>>(emptyList())
    private val _loadingState = MutableStateFlow(LoadingState())

    val viewMode: StateFlow<ViewMode> = settingsRepository.viewMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewMode.MEDIUM)

    val sortOrder: StateFlow<SortOrder> = settingsRepository.sortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NEWEST_FIRST)

    val articleFilter: StateFlow<ArticleFilter> = settingsRepository.articleFilter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleFilter.UNREAD_ONLY)

    // Combine raw articles with sort/filter settings to produce displayed list
    val uiState: StateFlow<ArticleListUiState> = combine(
        _rawArticles,
        _loadingState,
        settingsRepository.sortOrder,
        settingsRepository.articleFilter
    ) { articles, loading, sort, filter ->
        val filtered = when (filter) {
            ArticleFilter.UNREAD_ONLY -> articles.filter { !it.isRead }
            ArticleFilter.ALL -> articles
        }
        val sorted = when (sort) {
            SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.publishedTimestamp }
            SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.publishedTimestamp }
        }
        ArticleListUiState(
            articles = sorted,
            isLoading = loading.isLoading,
            isRefreshing = loading.isRefreshing,
            isLoadingMore = loading.isLoadingMore,
            continuation = loading.continuation,
            error = loading.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleListUiState())

    private var currentStreamId: String = ""
    private val markedReadIds = mutableSetOf<String>()

    fun loadArticles(streamId: String) {
        currentStreamId = streamId
        markedReadIds.clear()
        viewModelScope.launch {
            _loadingState.value = LoadingState(isLoading = true)
            val excludeRead = articleFilter.value == ArticleFilter.UNREAD_ONLY
            feedRepository.getArticles(streamId, excludeRead = excludeRead)
                .onSuccess { (articles, continuation) ->
                    _rawArticles.value = articles
                    _loadingState.value = LoadingState(continuation = continuation)
                    sharedArticleHolder.articles = articles
                }
                .onFailure { e ->
                    _loadingState.value = LoadingState(
                        error = e.localizedMessage ?: "Failed to load articles"
                    )
                }
        }
    }

    fun refresh() {
        markedReadIds.clear()
        viewModelScope.launch {
            _loadingState.value = _loadingState.value.copy(isRefreshing = true)
            val excludeRead = articleFilter.value == ArticleFilter.UNREAD_ONLY
            feedRepository.getArticles(currentStreamId, excludeRead = excludeRead)
                .onSuccess { (articles, continuation) ->
                    _rawArticles.value = articles
                    _loadingState.value = LoadingState(continuation = continuation)
                    sharedArticleHolder.articles = articles
                }
                .onFailure { e ->
                    _loadingState.value = _loadingState.value.copy(
                        isRefreshing = false,
                        error = e.localizedMessage
                    )
                }
        }
    }

    fun loadMore() {
        val continuation = _loadingState.value.continuation ?: return
        if (_loadingState.value.isLoadingMore) return

        viewModelScope.launch {
            _loadingState.value = _loadingState.value.copy(isLoadingMore = true)
            val excludeRead = articleFilter.value == ArticleFilter.UNREAD_ONLY
            feedRepository.getArticles(currentStreamId, continuation = continuation, excludeRead = excludeRead)
                .onSuccess { (newArticles, newContinuation) ->
                    _rawArticles.value = _rawArticles.value + newArticles
                    _loadingState.value = _loadingState.value.copy(
                        continuation = newContinuation,
                        isLoadingMore = false
                    )
                    sharedArticleHolder.articles = _rawArticles.value
                }
                .onFailure {
                    _loadingState.value = _loadingState.value.copy(isLoadingMore = false)
                }
        }
    }

    fun markAsRead(articleId: String) {
        if (markedReadIds.contains(articleId)) return
        val article = _rawArticles.value.find { it.id == articleId } ?: return
        if (article.isRead) return

        markedReadIds.add(articleId)
        // Update local state immediately
        _rawArticles.value = _rawArticles.value.map {
            if (it.id == articleId) it.copy(isRead = true) else it
        }
        // Sync with server
        viewModelScope.launch {
            feedRepository.markAsRead(articleId)
        }
    }

    /**
     * Called when articles scroll past the visible area.
     * Marks them as read both locally and on the server.
     */
    fun onArticlesScrolledPast(articleIds: List<String>) {
        val toMark = articleIds.filter { id ->
            !markedReadIds.contains(id) &&
                _rawArticles.value.find { it.id == id }?.isRead == false
        }
        if (toMark.isEmpty()) return

        markedReadIds.addAll(toMark)
        _rawArticles.value = _rawArticles.value.map { article ->
            if (article.id in toMark) article.copy(isRead = true) else article
        }

        viewModelScope.launch {
            toMark.forEach { id ->
                feedRepository.markAsRead(id)
            }
        }
    }

    fun toggleStar(articleId: String) {
        val article = _rawArticles.value.find { it.id == articleId } ?: return
        viewModelScope.launch {
            feedRepository.toggleStar(articleId, article.isStarred)
                .onSuccess {
                    _rawArticles.value = _rawArticles.value.map {
                        if (it.id == articleId) it.copy(isStarred = !it.isStarred) else it
                    }
                }
        }
    }
}

private data class LoadingState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val continuation: String? = null,
    val error: String? = null
)
