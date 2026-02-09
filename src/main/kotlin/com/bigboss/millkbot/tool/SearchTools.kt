package com.bigboss.millkbot.tool

import com.bigboss.millkbot.config.Config
import com.bigboss.millkbot.util.JsonUtil
import com.bigboss.millkbot.util.encode
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class SearchTools(
    private val restClient: RestClient,
    private val config: Config
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Tool(
        description = """
        使用Google搜索引擎搜索信息。

        参数说明：
        - query: 搜索关键词或问题

        返回格式：JSON数组，包含搜索结果，每个结果包含：
        - title: 标题
        - link: 链接
        - snippet: 摘要描述

        使用场景：
        - 需要获取最新的网络信息
        - 需要查找特定主题的资料
        - 需要验证某些事实或数据
    """
    )
    fun search(query: String): String {
        logger.debug("Tool search called: query={}", query)

        return try {
            if (query.isBlank()) {
                logger.warn("Empty query")
                return JsonUtil.encode(emptyList<SearchResult>())
            }

            val apiKey = config.searchConfig.serpApi
            if (apiKey.isBlank()) {
                logger.error("SerpApi key not configured")
                return JsonUtil.encode(emptyList<SearchResult>())
            }

            val response = restClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme("https")
                        .host("serpapi.com")
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("api_key", apiKey)
                        .queryParam("engine", "google")
                        .queryParam("num", 5)
                        .build()
                }
                .retrieve()
                .body(Map::class.java)

            val results = parseSearchResults(response)
            logger.debug("Tool search result: found {} results for query={}", results.size, query)

            JsonUtil.encode(results)
        } catch (e: Exception) {
            logger.error("Tool search failed: query={} error={}", query, e.message, e)
            JsonUtil.encode(emptyList<SearchResult>())
        }
    }

    private fun parseSearchResults(response: Map<*, *>?): List<SearchResult> {
        if (response == null) return emptyList()

        val organicResults = response["organic_results"] as? List<*> ?: return emptyList()

        return organicResults.mapNotNull { result ->
            val resultMap = result as? Map<*, *> ?: return@mapNotNull null
            SearchResult(
                title = resultMap["title"] as? String ?: "",
                link = resultMap["link"] as? String ?: "",
                snippet = resultMap["snippet"] as? String ?: ""
            )
        }
    }

    @Serializable
    data class SearchResult(
        val title: String,
        val link: String,
        val snippet: String
    )
}
