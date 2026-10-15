package com.example.vidhisetu

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://vidhisetu-backend.onrender.com/"

    val legalNewsApi: LegalNewsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(LegalNewsApi::class.java)
    }
}