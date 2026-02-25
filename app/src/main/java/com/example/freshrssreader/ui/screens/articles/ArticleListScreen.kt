package com.example.freshrssreader.ui.screens.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.freshrssreader.data.repository.ViewMode
import com.example.freshrssreader.ui.components.ArticleCardCompact
import com.example.freshrssreader.ui.components.ArticleCardFull
import com.example.freshrssreader.ui.components.ArticleCardMedium
import com.example.freshrssreader.ui.components.SwipeToMarkRead
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ArticleListScreen(
    streamId: String,
    title: String,
    onArticleClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(streamId) {
        viewModel.loadArticles(streamId)
    }

    // Pagination trigger
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 5 && !uiState.isLoadingMore && uiState.continuation != null
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    // Auto-mark articles as read when they scroll above the visible area
    LaunchedEffect(listState) {
        snapshotFlow {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val articles = uiState.articles
            // All articles above the first visible item have been scrolled past
            if (firstVisibleIndex > 0 && articles.isNotEmpty()) {
                articles.take(firstVisibleIndex).map { it.id }
            } else {
                emptyList()
            }
        }
            .debounce(300)
            .collectLatest { scrolledPastIds ->
                if (scrolledPastIds.isNotEmpty()) {
                    viewModel.onArticlesScrolledPast(scrolledPastIds)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.articles.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.articles.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = uiState.articles,
                            key = { _, article -> article.id }
                        ) { index, article ->
                            SwipeToMarkRead(
                                onMarkRead = { viewModel.markAsRead(article.id) }
                            ) {
                                when (viewMode) {
                                    ViewMode.COMPACT -> ArticleCardCompact(
                                        article = article,
                                        onClick = { onArticleClick(article.id) },
                                        onStarToggle = { viewModel.toggleStar(article.id) },
                                        onReadToggle = { viewModel.toggleReadUnread(article.id) }
                                    )
                                    ViewMode.MEDIUM -> ArticleCardMedium(
                                        article = article,
                                        onClick = { onArticleClick(article.id) },
                                        onStarToggle = { viewModel.toggleStar(article.id) },
                                        onReadToggle = { viewModel.toggleReadUnread(article.id) }
                                    )
                                    ViewMode.FULL -> ArticleCardFull(
                                        article = article,
                                        onClick = { onArticleClick(article.id) },
                                        onStarToggle = { viewModel.toggleStar(article.id) },
                                        onReadToggle = { viewModel.toggleReadUnread(article.id) }
                                    )
                                }
                            }
                            HorizontalDivider()
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
