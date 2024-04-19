package com.assignment.unsplashimagegrid

import com.google.gson.annotations.SerializedName


data class UnsplashPhoto(
    val urls: Urls
)

data class Urls(
    @SerializedName("regular")
    val regularUrl: String
)

