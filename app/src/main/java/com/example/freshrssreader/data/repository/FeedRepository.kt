package com.example.freshrssreader.data.repository

import com.example.freshrssreader.data.api.GoogleReaderApi
import com.example.freshrssreader.data.api.dto.StreamItemDto
import com.example.freshrssreader.data.model.Article
import com.example.freshrssreader.data.model.Category
import com.example.freshrssreader.data.model.Feed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val api: GoogleReaderApi
) {
    companion object {
        const val STATE_READ = "user/-/state/com.google/read"
        const val STATE_STARRED = "user/-/state/com.google/starred"
        const val STATE_READING_LIST = "user/-/state/com.google/reading-list"
        const val LABEL_PREFIX = "user/-/label/"
    }

    suspend fun getSubscriptions(): Result<List<Feed>> = runCatching {
        val subsResponse = api.getSubscriptions()
        val unreadResponse = api.getUnreadCounts()

        if (!subsResponse.isSuccessful) {
            throw Exception("Failed to fetch subscriptions: ${subsResponse.code()}")
        }

        val unreadMap = unreadResponse.body()?.unreadcounts
            ?.associate { it.id to it.count } ?: emptyMap()

        subsResponse.body()?.subscriptions?.map { sub ->
            Feed(
                id = sub.id,
                title = sub.title,
                url = sub.url,
                htmlUrl = sub.htmlUrl,
                iconUrl = sub.iconUrl,
                categories = sub.categories?.map { cat ->
                    Category(id = cat.id, label = cat.label ?: cat.id.substringAfterLast("/"))
                } ?: emptyList(),
                unreadCount = unreadMap[sub.id] ?: 0
            )
        } ?: emptyList()
    }

    suspend fun getArticles(
        streamId: String,
        count: Int = 50,
        continuation: String? = null,
        excludeRead: Boolean = false,
        oldestFirst: Boolean = false
    ): Result<Pair<List<Article>, String?>> = runCatching {
        val excludeTarget = if (excludeRead) STATE_READ else null
        val ranking = if (oldestFirst) "o" else null
        val response = api.getStreamContents(streamId, count, continuation, excludeTarget, ranking)
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch articles: ${response.code()}")
        }
        val body = response.body() ?: throw Exception("Empty response body")
        val articles = body.items?.map { it.toArticle() } ?: emptyList()
        articles to body.continuation
    }

    suspend fun getStarredArticles(
        count: Int = 50,
        continuation: String? = null
    ): Result<Pair<List<Article>, String?>> {
        return getArticles(STATE_STARRED, count, continuation)
    }

    suspend fun markAsRead(articleId: String): Result<Unit> = runCatching {
        val response = api.editTag(itemId = articleId, addTag = STATE_READ)
        if (!response.isSuccessful) {
            throw Exception("Failed to mark as read: ${response.code()}")
        }
    }

    suspend fun markAsUnread(articleId: String): Result<Unit> = runCatching {
        val response = api.editTag(itemId = articleId, removeTag = STATE_READ)
        if (!response.isSuccessful) {
            throw Exception("Failed to mark as unread: ${response.code()}")
        }
    }

    suspend fun toggleStar(articleId: String, currentlyStarred: Boolean): Result<Unit> = runCatching {
        val response = if (currentlyStarred) {
            api.editTag(itemId = articleId, removeTag = STATE_STARRED)
        } else {
            api.editTag(itemId = articleId, addTag = STATE_STARRED)
        }
        if (!response.isSuccessful) {
            throw Exception("Failed to toggle star: ${response.code()}")
        }
    }

    private fun StreamItemDto.toArticle(): Article {
        val articleUrl = canonical?.firstOrNull()?.href
            ?: alternate?.firstOrNull()?.href

        val htmlContent = content?.content ?: summary?.content
        val excerpt = htmlContent?.let { html ->
            html.replace(Regex("<[^>]*>"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
                .take(200)
        }

        val imageUrl = enclosure?.firstOrNull { it.type?.startsWith("image/") == true }?.href
            ?: htmlContent?.let {
                Regex("""<img[^>]+src=["']([^"']+)["']""").find(it)?.groupValues?.get(1)
            }

        val isRead = categories?.contains(STATE_READ) == true
        val isStarred = categories?.contains(STATE_STARRED) == true

        return Article(
            id = id,
            title = title ?: "",
            url = articleUrl,
            author = author,
            publishedTimestamp = published ?: 0L,
            feedTitle = origin?.title,
            contentHtml = htmlContent,
            excerpt = excerpt,
            imageUrl = imageUrl,
            isRead = isRead,
            isStarred = isStarred
        )
    }
}
