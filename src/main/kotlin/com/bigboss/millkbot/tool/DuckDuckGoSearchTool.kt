package com.bigboss.millkbot.tool

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.ai.tool.annotation.Tool
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DuckDuckGoSearchTool {

    private val httpClient = HttpClient(CIO)
    private val objectMapper = ObjectMapper()

    @Tool(
        description = """使用 DuckDuckGo 搜索引擎搜索互联网信息。
        当用户询问需要实时信息、最新资讯、网络搜索或你不确定的事实时使用此工具。
        输入应该是一个搜索查询字符串。
        返回搜索结果的摘要，包括标题、描述和相关链接。"""
    )
    fun search(query: String): String {
        return runBlocking {
            try {
                val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                val url = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"

                val response: HttpResponse = httpClient.get(url)
                val responseBody = response.bodyAsText()

                val result = objectMapper.readValue(responseBody, DuckDuckGoResponse::class.java)

                formatSearchResult(query, result)
            } catch (e: Exception) {
                "搜索失败: ${e.message}"
            }
        }
    }

    private fun formatSearchResult(query: String, result: DuckDuckGoResponse): String {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DuckDuckGoResponse(
        val abstract: String? = null,
        val abstractText: String? = null,
        val abstractSource: String? = null,
        val abstractURL: String? = null,
        val answer: String? = null,
        val answerType: String? = null,
        val definition: String? = null,
        val definitionSource: String? = null,
        val definitionURL: String? = null,
        val heading: String? = null,
        val image: String? = null,
        val relatedTopics: List<RelatedTopic>? = null,
        val results: List<Result>? = null,
        val type: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RelatedTopic(
        val text: String? = null,
        val firstURL: String? = null,
        val icon: Icon? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Icon(
        val url: String? = null,
        val height: Int? = null,
        val width: Int? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Result(
        val text: String? = null,
        val firstURL: String? = null,
        val icon: Icon? = null
    )
}
