package com.example.freshrssreader.data.model

data class Article(
    val id: String,
    val title: String,
    val url: String?,
    val author: String?,
    val publishedTimestamp: Long,
    val feedTitle: String?,
    val contentHtml: String?,
    val excerpt: String?,
    val imageUrl: String?,
    val isRead: Boolean,
    val isStarred: Boolean
)
