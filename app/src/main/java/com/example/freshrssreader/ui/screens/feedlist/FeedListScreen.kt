package com.example.freshrssreader.ui.screens.feedlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.freshrssreader.data.model.Feed
import com.example.freshrssreader.data.repository.FeedRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListScreen(
    onFeedClick: (streamId: String, title: String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: FeedListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FreshRSS Reader") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.feeds.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.feeds.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // All Articles
                        item {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "All Articles",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.RssFeed,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    if (uiState.totalUnread > 0) {
                                        Badge {
                                            Text("${uiState.totalUnread}")
                                        }
                                    }
                                },
                                modifier = Modifier.clickable {
                                    onFeedClick(
                                        FeedRepository.STATE_READING_LIST,
                                        "All Articles"
                                    )
                                }
                            )
                        }

                        // Starred
                        item {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Starred",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onFeedClick(
                                        FeedRepository.STATE_STARRED,
                                        "Starred"
                                    )
                                }
                            )
                            HorizontalDivider()
                        }

                        // Categories with feeds
                        uiState.feedsByCategory.forEach { (category, feeds) ->
                            item {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            category.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingContent = {
                                        val categoryUnread = feeds.sumOf { it.unreadCount }
                                        if (categoryUnread > 0) {
                                            Badge {
                                                Text("$categoryUnread")
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        onFeedClick(category.id, category.label)
                                    }
                                )
                            }

                            items(feeds) { feed ->
                                FeedItem(
                                    feed = feed,
                                    onClick = { onFeedClick(feed.id, feed.title) },
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            item { HorizontalDivider() }
                        }

                        // Uncategorized feeds
                        if (uiState.uncategorizedFeeds.isNotEmpty()) {
                            item {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "Uncategorized",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )
                            }

                            items(uiState.uncategorizedFeeds) { feed ->
                                FeedItem(
                                    feed = feed,
                                    onClick = { onFeedClick(feed.id, feed.title) },
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedItem(
    feed: Feed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(feed.title) },
        leadingContent = {
            Row {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.RssFeed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            if (feed.unreadCount > 0) {
                Badge {
                    Text("${feed.unreadCount}")
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}
