package com.bigboss.millkbot.tool

import com.bigboss.millkbot.util.JsonUtil
import com.bigboss.millkbot.util.decode
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
class SearchTools(
    private val jsonUtil: JsonUtil,
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = HttpClient(CIO)

    @Tool(description = "使用 DuckDuckGo 搜索引擎搜索互联网信息。当用户询问需要实时信息、最新资讯、网络搜索或你不确定的事实时使用此工具。输入应该是一个搜索查询字符串。返回搜索结果的摘要，包括标题、描述和相关链接。")
    fun duckDuckGoSearch(request: SearchRequest): String {
        return runBlocking {
            try {
                logger.debug("Tool duckDuckGoSearch called: query={}", request.query)
                val encodedQuery = URLEncoder.encode(request.query, StandardCharsets.UTF_8.toString())
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

                val result = jsonUtil.decode<DuckDuckGoResponse>(responseBody)
                val formatted = formatSearchResult(request.query, result)
                logger.debug("Tool duckDuckGoSearch formatted result: length={}", formatted.length)
                formatted
            } catch (e: Exception) {
                logger.debug("Tool duckDuckGoSearch failed: query={} error={}", request.query, e.message)
                "搜索失败: ${e.message}"
            }
        }
    }

    fun formatSearchResult(query: String, result: DuckDuckGoResponse): String {
        val sb = StringBuilder()
        sb.append("搜索查询: $query\n\n")

        if (!result.abstract.isNullOrBlank()) {
            sb.append("摘要:\n${result.abstract}\n")
            if (!result.abstractURL.isNullOrBlank()) {
                sb.append("来源: ${result.abstractURL}\n")
            }
            sb.append("\n")
        }

        if (!result.definition.isNullOrBlank()) {
            sb.append("定义:\n${result.definition}\n")
            if (!result.definitionURL.isNullOrBlank()) {
                sb.append("来源: ${result.definitionURL}\n")
            }
            sb.append("\n")
        }

        if (!result.relatedTopics.isNullOrEmpty()) {
            sb.append("相关主题:\n")
            result.relatedTopics.take(5).forEach { topic ->
                if (!topic.text.isNullOrBlank()) {
                    sb.append("- ${topic.text}\n")
                    if (!topic.firstURL.isNullOrBlank()) {
                        sb.append("  链接: ${topic.firstURL}\n")
                    }
                }
            }
            sb.append("\n")
        }

        if (!result.answer.isNullOrBlank()) {
            sb.append("答案: ${result.answer}\n\n")
        }

        if (sb.length <= "搜索查询: $query\n\n".length) {
            return "未找到关于 '$query' 的相关信息。建议尝试不同的搜索词。"
        }

        return sb.toString().trim()
    }

    @Serializable
    data class SearchRequest(
        val query: String
    )

    @Serializable
    data class DuckDuckGoResponse(
        @SerialName("Abstract")
        val abstract: String? = null,
        @SerialName("AbstractText")
        val abstractText: String? = null,
        @SerialName("AbstractSource")
        val abstractSource: String? = null,
        @SerialName("AbstractURL")
        val abstractURL: String? = null,
        @SerialName("Answer")
        val answer: String? = null,
        @SerialName("AnswerType")
        val answerType: String? = null,
        @SerialName("Definition")
        val definition: String? = null,
        @SerialName("DefinitionSource")
        val definitionSource: String? = null,
        @SerialName("DefinitionURL")
        val definitionURL: String? = null,
        @SerialName("Heading")
        val heading: String? = null,
        @SerialName("Image")
        val image: String? = null,
        @SerialName("RelatedTopics")
        val relatedTopics: List<RelatedTopic>? = null,
        @SerialName("Results")
        val results: List<Result>? = null,
        @SerialName("Type")
        val type: String? = null
    )

    @Serializable
    data class RelatedTopic(
        @SerialName("Text")
        val text: String? = null,
        @SerialName("FirstURL")
        val firstURL: String? = null,
        @SerialName("Icon")
        val icon: Icon? = null,
        @SerialName("Topics")
        val topics: List<RelatedTopic>? = null
    )

    @Serializable
    data class Icon(
        @SerialName("URL")
        val url: String? = null,
        @SerialName("Height")
        val height: Int? = null,
        @SerialName("Width")
        val width: Int? = null
    )

    @Serializable
    data class Result(
        @SerialName("Text")
        val text: String? = null,
        @SerialName("FirstURL")
        val firstURL: String? = null,
        @SerialName("Icon")
        val icon: Icon? = null
    )
}
