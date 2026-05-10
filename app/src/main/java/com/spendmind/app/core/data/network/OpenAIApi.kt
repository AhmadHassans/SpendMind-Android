package com.spendmindai.app.core.data.network

import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApi {

    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: ChatRequest): ChatResponse
}
