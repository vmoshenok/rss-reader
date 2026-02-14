package com.example.freshrssreader.ui.screens.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshrssreader.data.model.Article
import com.example.freshrssreader.data.repository.FeedRepository
import com.example.freshrssreader.data.repository.SettingsRepository
import com.example.freshrssreader.data.repository.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    val viewMode: StateFlow<ViewMode> = settingsRepository.viewMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewMode.MEDIUM)

    private var currentStreamId: String = ""

    fun loadArticles(streamId: String) {
        currentStreamId = streamId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            feedRepository.getArticles(streamId)
                .onSuccess { (articles, continuation) ->
                    _uiState.value = ArticleListUiState(
                        articles = articles,
                        continuation = continuation,
                        isLoading = false
                    )
                    sharedArticleHolder.articles = articles
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load articles"
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            feedRepository.getArticles(currentStreamId)
                .onSuccess { (articles, continuation) ->
                    _uiState.value = ArticleListUiState(
                        articles = articles,
                        continuation = continuation,
                        isRefreshing = false
                    )
                    sharedArticleHolder.articles = articles
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = e.localizedMessage
                    )
                }
        }
    }

    fun loadMore() {
        val continuation = _uiState.value.continuation ?: return
        if (_uiState.value.isLoadingMore) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            feedRepository.getArticles(currentStreamId, continuation = continuation)
                .onSuccess { (newArticles, newContinuation) ->
                    val allArticles = _uiState.value.articles + newArticles
                    _uiState.value = _uiState.value.copy(
                        articles = allArticles,
                        continuation = newContinuation,
                        isLoadingMore = false
                    )
                    sharedArticleHolder.articles = allArticles
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
        }
    }

    fun markAsRead(articleId: String) {
        viewModelScope.launch {
            feedRepository.markAsRead(articleId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        articles = _uiState.value.articles.map {
                            if (it.id == articleId) it.copy(isRead = true) else it
                        }
                    )
                }
        }
    }

    fun toggleStar(articleId: String) {
        val article = _uiState.value.articles.find { it.id == articleId } ?: return
        viewModelScope.launch {
            feedRepository.toggleStar(articleId, article.isStarred)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        articles = _uiState.value.articles.map {
                            if (it.id == articleId) it.copy(isStarred = !it.isStarred) else it
                        }
                    )
                }
        }
    }
}
