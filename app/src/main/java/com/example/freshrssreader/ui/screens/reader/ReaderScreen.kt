package com.example.freshrssreader.ui.screens.reader

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.freshrssreader.data.repository.BrowserMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    articleId: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val article by viewModel.article.collectAsState()
    val browserMode by viewModel.browserMode.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    // If external browser mode, open URL and go back
    LaunchedEffect(article, browserMode) {
        if (browserMode == BrowserMode.EXTERNAL && article?.url != null) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article!!.url)))
            onBack()
        }
    }

    // Don't show UI if external browser mode
    if (browserMode == BrowserMode.EXTERNAL) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = article?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleStar() }) {
                        Icon(
                            imageVector = if (article?.isStarred == true) Icons.Default.Star
                                else Icons.Default.StarBorder,
                            contentDescription = if (article?.isStarred == true) "Unstar" else "Star",
                            tint = if (article?.isStarred == true) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        article?.url?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Open in browser"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        val currentArticle = article
        val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        if (currentArticle?.url != null) {
            key(isDarkTheme) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            if (Build.VERSION.SDK_INT >= 33) {
                                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isDarkTheme)
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                                    @Suppress("DEPRECATION")
                                    WebSettingsCompat.setForceDark(
                                        settings,
                                        if (isDarkTheme) WebSettingsCompat.FORCE_DARK_ON
                                        else WebSettingsCompat.FORCE_DARK_OFF
                                    )
                                }
                            }
                            loadUrl(currentArticle.url)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No URL available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
