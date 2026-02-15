package com.example.freshrssreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.freshrssreader.data.model.Article

@Composable
fun ArticleCardMedium(
    article: Article,
    onClick: () -> Unit,
    onStarToggle: () -> Unit,
    onReadToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (article.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentAlpha = if (article.isRead) 0.5f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Read/unread book icon
        IconButton(onClick = onReadToggle, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (article.isRead) Icons.AutoMirrored.Filled.MenuBook
                    else Icons.Default.Book,
                contentDescription = if (article.isRead) "Mark as unread" else "Mark as read",
                tint = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (article.imageUrl != null) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .alpha(contentAlpha),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (!article.isRead) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (article.feedTitle != null) {
                    Text(
                        text = article.feedTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = formatDate(article.publishedTimestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (article.isRead) 0.5f else 1f
                    )
                )
            }
        }

        IconButton(onClick = onStarToggle) {
            Icon(
                imageVector = if (article.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (article.isStarred) "Unstar" else "Star",
                tint = if (article.isStarred) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (article.isRead) 0.5f else 1f
                    )
            )
        }
    }
}
