package com.example.freshrssreader.data.api.dto

import com.google.gson.annotations.SerializedName

data class SubscriptionListResponse(
    val subscriptions: List<SubscriptionDto>
)

data class SubscriptionDto(
    val id: String,
    val title: String,
    val url: String?,
    val htmlUrl: String?,
    @SerializedName("iconUrl") val iconUrl: String?,
    val categories: List<CategoryDto>?
)

data class CategoryDto(
    val id: String,
    val label: String?
)
