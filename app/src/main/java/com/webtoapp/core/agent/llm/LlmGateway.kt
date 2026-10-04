package com.webtoapp.core.agent.llm

import android.content.Context
import com.webtoapp.data.model.AiProvider
import kotlinx.coroutines.flow.Flow

interface LlmGateway {
    fun chatStream(req: ChatRequest): Flow<LlmEvent>
}

internal interface LlmProvider {
    fun supports(provider: AiProvider): Boolean

    /**
     * Format-aware match: a CUSTOM endpoint is routed by its declared [com.webtoapp.data.model.ApiFormat]
     * (Anthropic Messages / Chat Completions / Responses), not by the enum alone.
     */
    fun supports(req: ChatRequest): Boolean = supports(req.apiKey.provider)
    fun chatStream(req: ChatRequest): Flow<LlmEvent>
}

class DefaultLlmGateway internal constructor(private val providers: List<LlmProvider>) : LlmGateway {
    override fun chatStream(req: ChatRequest): Flow<LlmEvent> {
        val provider = providers.firstOrNull { it.supports(req) }
            ?: providers.first { it.supports(AiProvider.OPENAI) }
        return provider.chatStream(req)
    }
    companion object {
        fun create(context: Context): LlmGateway = DefaultLlmGateway(
            listOf(AnthropicProvider(context), GeminiProvider(context), OllamaProvider(context), ResponsesProvider(context), OpenAiCompatProvider(context))
        )
    }
}
