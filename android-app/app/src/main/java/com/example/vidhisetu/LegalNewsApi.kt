package com.example.vidhisetu

import retrofit2.Call
import retrofit2.http.GET

interface LegalNewsApi {

    @GET("api/legal-news")
    fun getLegalNews(): Call<LegalNewsResponse>
}