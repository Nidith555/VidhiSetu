package com.example.vidhisetu

data class LegalNewsResponse(
    val success: Boolean,
    val count: Int,
    val news: List<LegalNewsItem>
)