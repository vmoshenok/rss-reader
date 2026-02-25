package com.example.freshrssreader.data.api

import com.example.freshrssreader.data.api.dto.StreamContentsResponse
import com.example.freshrssreader.data.api.dto.SubscriptionListResponse
import com.example.freshrssreader.data.api.dto.TagListResponse
import com.example.freshrssreader.data.api.dto.UnreadCountResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleReaderApi {

    @FormUrlEncoded
    @POST("accounts/ClientLogin")
    suspend fun login(
        @Field("Email") email: String,
        @Field("Passwd") password: String
    ): Response<ResponseBody>

    @GET("reader/api/0/token")
    suspend fun getToken(): Response<ResponseBody>

    @GET("reader/api/0/subscription/list?output=json")
    suspend fun getSubscriptions(): Response<SubscriptionListResponse>

    @GET("reader/api/0/tag/list?output=json")
    suspend fun getTagList(): Response<TagListResponse>

    @GET("reader/api/0/unread-count?output=json")
    suspend fun getUnreadCounts(): Response<UnreadCountResponse>

    @GET("reader/api/0/stream/contents/{streamId}?output=json")
    suspend fun getStreamContents(
        @Path("streamId", encoded = true) streamId: String,
        @Query("n") count: Int = 50,
        @Query("c") continuation: String? = null,
        @Query("xt") excludeTarget: String? = null,
        @Query("r") ranking: String? = null
    ): Response<StreamContentsResponse>

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    suspend fun editTag(
        @Field("i") itemId: String,
        @Field("a") addTag: String? = null,
        @Field("r") removeTag: String? = null
    ): Response<ResponseBody>
}
