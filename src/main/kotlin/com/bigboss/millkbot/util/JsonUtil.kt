package com.bigboss.millkbot.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component

@Component
class JsonUtil {

    @OptIn(ExperimentalSerializationApi::class)
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
        explicitNulls = false
    }
}

inline fun <reified T> JsonUtil.decode(text: String): T {
    return json.decodeFromString(text)
}

inline fun <reified T> JsonUtil.encode(value: T): String {
    return json.encodeToString(value)
}
