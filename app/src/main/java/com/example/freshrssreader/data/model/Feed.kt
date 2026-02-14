package com.example.freshrssreader.data.model

data class Feed(
    val id: String,
    val title: String,
    val url: String?,
    val htmlUrl: String?,
    val iconUrl: String?,
    val categories: List<Category>,
    val unreadCount: Int = 0
)
