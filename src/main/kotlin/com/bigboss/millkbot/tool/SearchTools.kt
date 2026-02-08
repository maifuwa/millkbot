package com.bigboss.millkbot.tool

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class SearchTools {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = HttpClient(CIO)

    @PreDestroy
    fun close() {
        httpClient.close()
    }

    @Tool(
        description = """
        当用户询问需要实时信息、最新资讯、网络搜索或你不确定的事实时使用此工具。

        参数说明：
        - query: 搜索查询字符串

        返回格式：搜索结果的文本摘要，包括标题、描述和相关链接
    """
    )
    suspend fun search(query: String): String {
        return try {
            logger.debug("Tool search called: query={}", query)
            val encodedQuery = withContext(Dispatchers.IO) {
                URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            }
            val url = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"

            val startMs = System.currentTimeMillis()
            logger.debug("Tool search request: url={}", url)

            val response: HttpResponse = httpClient.get(url)
            val responseBody = response.bodyAsText()

            val durationMs = System.currentTimeMillis() - startMs
            logger.debug(
                "Tool search response: status={} durationMs={} bodyLength={}",
                response.status,
                durationMs,
                responseBody.length
            )

            responseBody
        } catch (e: Exception) {
            logger.debug("Tool search failed: query={} error={}", query, e.message)
            "搜索失败: ${e.message}"
        }
    }
}
