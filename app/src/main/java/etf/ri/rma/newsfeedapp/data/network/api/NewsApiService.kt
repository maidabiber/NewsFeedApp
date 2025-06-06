package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.data.network.NewsApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {

    @GET("news/top")
    suspend fun getNews(
        @Query("api_token") apiKey: String,
        @Query("categories") categories: String,
        @Query("limit") limit: Int
    ): NewsApiResponse

    @GET("news/similar/{uuid}")
    suspend fun getSimilarStories(
        @Path("uuid") uuid: String,
        @Query("api_token") apiKey: String
    ): NewsApiResponse

}