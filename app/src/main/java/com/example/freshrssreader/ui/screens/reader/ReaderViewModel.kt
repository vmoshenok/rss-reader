package com.example.freshrssreader.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshrssreader.data.model.Article
import com.example.freshrssreader.data.repository.FeedRepository
import com.example.freshrssreader.data.repository.BrowserMode
import com.example.freshrssreader.data.repository.SettingsRepository
import com.example.freshrssreader.ui.screens.articles.SharedArticleHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val sharedArticleHolder: SharedArticleHolder,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    val browserMode: StateFlow<BrowserMode> = settingsRepository.browserMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BrowserMode.INTERNAL)

    fun loadArticle(index: Int) {
        val articles = sharedArticleHolder.articles
        if (index in articles.indices) {
            _article.value = articles[index]
        }
    }

    fun toggleStar() {
        val current = _article.value ?: return
        viewModelScope.launch {
            feedRepository.toggleStar(current.id, current.isStarred)
                .onSuccess {
                    _article.value = current.copy(isStarred = !current.isStarred)
                }
        }
    }
}
