package com.jetbrains.rider.plugins.efcore.features.shared.services

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.annotations.NonNls
import java.io.File

@Service(Service.Level.APP)
class JsonSerializer {
    companion object {
        private val json =
            jacksonObjectMapper()
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())

        fun getInstance() = service<JsonSerializer>()
    }

    private val logger = logger<JsonSerializer>()

    fun deserializeNode(jsonString: String): JsonNode? {
        return try {
            json.readTree(jsonString)
        } catch (e: JacksonException) {
            @NonNls
            val message = "Input JSON is invalid"
            logger.error(message, e)
            null
        }
    }

    fun deserializeNode(jsonFile: File): JsonNode? {
        return try {
            json.readTree(jsonFile)
        } catch (e: JacksonException) {
            @NonNls
            val message = "Input JSON file '${jsonFile.absolutePath}' is invalid"
            logger.error(message, e)
            null
        }
    }
}