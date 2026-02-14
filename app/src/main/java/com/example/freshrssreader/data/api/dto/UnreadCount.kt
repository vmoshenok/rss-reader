package com.example.freshrssreader.data.api.dto

data class UnreadCountResponse(
    val max: Int?,
    val unreadcounts: List<UnreadCountDto>?
)

data class UnreadCountDto(
    val id: String,
    val count: Int,
    val newestItemTimestampUsec: String?
)
