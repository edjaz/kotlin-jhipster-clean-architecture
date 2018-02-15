package fr.edjaz.config

import java.io.IOException

import org.elasticsearch.client.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.EntityMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

@Configuration
class ElasticsearchConfiguration {

    @Bean
    fun elasticsearchTemplate(client: Client, jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder): ElasticsearchTemplate {
        return ElasticsearchTemplate(client, CustomEntityMapper(jackson2ObjectMapperBuilder.createXmlMapper(false).build()))
    }

    inner class CustomEntityMapper(private val objectMapper: ObjectMapper) : EntityMapper {

        init {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        }

        @Throws(IOException::class)
        override fun mapToString(`object`: Any): String {
            return objectMapper.writeValueAsString(`object`)
        }

        @Throws(IOException::class)
        override fun <T> mapToObject(source: String, clazz: Class<T>): T {
            return objectMapper.readValue(source, clazz)
        }
    }
}
