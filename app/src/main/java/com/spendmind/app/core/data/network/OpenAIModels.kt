package com.spendmindai.app.core.data.network

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String = "gpt-4o-mini",
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("temperature")
    val temperature: Double = 0.2,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1024
)

data class Message(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
) {
    companion object {
        fun system(content: String): Message = Message(role = "system", content = content)
        fun user(content: String): Message = Message(role = "user", content = content)
        fun assistant(content: String): Message = Message(role = "assistant", content = content)
    }
}

data class ChatResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("object")
    val objectType: String?,
    @SerializedName("created")
    val created: Long?,
    @SerializedName("model")
    val model: String?,
    @SerializedName("choices")
    val choices: List<Choice>?,
    @SerializedName("usage")
    val usage: Usage?
) {
    fun getFirstContent(): String? =
        choices?.firstOrNull()?.message?.content
}

data class Choice(
    @SerializedName("index")
    val index: Int?,
    @SerializedName("message")
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?
)
