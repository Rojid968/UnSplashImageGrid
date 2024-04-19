package com.assignment.unsplashimagegrid

import retrofit2.http.GET

interface UnsplashApi {
    @GET("photos/?client_id=dkaPu5hF02cn3e5-sTpu-NcUGq31Zb5necivfx6nDho")
    suspend fun getPhotos(
        @retrofit2.http.Query("per_page") perPage: Int,
        @retrofit2.http.Query("page") page: Int
    ): List<UnsplashPhoto>
}