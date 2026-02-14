package com.example.freshrssreader.data.api.dto

data class TagListResponse(
    val tags: List<TagDto>?
)

data class TagDto(
    val id: String,
    val type: String?
)
