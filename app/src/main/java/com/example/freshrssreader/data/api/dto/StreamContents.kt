package com.example.freshrssreader.data.api.dto

data class StreamContentsResponse(
    val id: String?,
    val title: String?,
    val continuation: String?,
    val items: List<StreamItemDto>?
)

data class StreamItemDto(
    val id: String,
    val title: String?,
    val published: Long?,
    val author: String?,
    val canonical: List<LinkDto>?,
    val alternate: List<LinkDto>?,
    val summary: SummaryDto?,
    val content: SummaryDto?,
    val origin: OriginDto?,
    val categories: List<String>?,
    val enclosure: List<EnclosureDto>?
)

data class LinkDto(
    val href: String?
)

data class SummaryDto(
    val content: String?
)

data class OriginDto(
    val streamId: String?,
    val title: String?,
    val htmlUrl: String?
)

data class EnclosureDto(
    val href: String?,
    val type: String?
)
