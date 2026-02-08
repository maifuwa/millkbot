package com.bigboss.millkbot.tool

import com.bigboss.millkbot.util.JsonUtil
import com.bigboss.millkbot.util.decode
import com.bigboss.millkbot.util.encode
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class SearchTools {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = HttpClient(CIO)

    @Tool(description = "使用 DuckDuckGo 搜索引擎搜索互联网信息。当用户询问需要实时信息、最新资讯、网络搜索或你不确定的事实时使用此工具。输入应该是一个搜索查询字符串。返回搜索结果的摘要，包括标题、描述和相关链接。")
    fun duckDuckGoSearch(query: String): String {
        return runBlocking {
            try {
                logger.debug("Tool duckDuckGoSearch called: query={}", query)
                val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                val url = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"

                val startMs = System.currentTimeMillis()
                logger.debug("Tool duckDuckGoSearch request: url={}", url)

                val response: HttpResponse = httpClient.get(url)
                val responseBody = response.bodyAsText()

                val durationMs = System.currentTimeMillis() - startMs
                logger.debug(
                    "Tool duckDuckGoSearch response: status={} durationMs={} bodyLength={}",
                    response.status,
                    durationMs,
                    responseBody.length
                )

                val result = JsonUtil.decode<DuckDuckGoResponse>(responseBody)
                val searchResults = formatSearchResult(query, result)
                val jsonResponse = JsonUtil.encode(searchResults)
                logger.debug("Tool duckDuckGoSearch formatted result: length={}", jsonResponse.length)
                jsonResponse
            } catch (e: Exception) {
                logger.debug("Tool duckDuckGoSearch failed: query={} error={}", query, e.message)
                JsonUtil.encode(listOf(SearchResponse("错误", "搜索失败: ${e.message}", "")))
            }
        }
    }

    fun formatSearchResult(query: String, result: DuckDuckGoResponse): List<SearchResponse> {
        val results = mutableListOf<SearchResponse>()

        if (!result.abstract.isNullOrBlank()) {
            results.add(
                SearchResponse(
                    title = result.heading ?: "摘要",
                    description = result.abstract,
                    url = result.abstractURL ?: ""
                )
            )
        }

        if (!result.definition.isNullOrBlank()) {
            results.add(
                SearchResponse(
                    title = "定义",
                    description = result.definition,
                    url = result.definitionURL ?: ""
                )
            )
        }

        if (!result.answer.isNullOrBlank()) {
            results.add(
                SearchResponse(
                    title = "答案",
                    description = result.answer,
                    url = ""
                )
            )
        }

        if (!result.relatedTopics.isNullOrEmpty()) {
            result.relatedTopics.take(5).forEach { topic ->
                if (!topic.text.isNullOrBlank()) {
                    results.add(
                        SearchResponse(
                            title = "相关主题",
                            description = topic.text,
                            url = topic.firstURL ?: ""
                        )
                    )
                }
            }
        }

        if (results.isEmpty()) {
            results.add(
                SearchResponse(
                    title = "无结果",
                    description = "未找到关于 '$query' 的相关信息。建议尝试不同的搜索词。",
                    url = ""
                )
            )
        }

        return results
    }

    @Serializable
    data class SearchResponse(
        val title: String,
        val description: String,
        val url: String
    )

    @Serializable
    data class DuckDuckGoResponse(
        @SerialName("Abstract")
        val abstract: String? = null,
        @SerialName("AbstractURL")
        val abstractURL: String? = null,
        @SerialName("Answer")
        val answer: String? = null,
        @SerialName("Definition")
        val definition: String? = null,
        @SerialName("DefinitionURL")
        val definitionURL: String? = null,
        @SerialName("Heading")
        val heading: String? = null,
        @SerialName("RelatedTopics")
        val relatedTopics: List<RelatedTopic>? = null
    )

    @Serializable
    data class RelatedTopic(
        @SerialName("Text")
        val text: String? = null,
        @SerialName("FirstURL")
        val firstURL: String? = null
    )
}
