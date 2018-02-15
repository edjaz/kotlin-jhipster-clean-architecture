package fr.edjaz.config.elasticsearch

import java.lang.System.currentTimeMillis

import javax.annotation.PostConstruct

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.stereotype.Component

@Component
class IndexReinitializer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val elasticsearchTemplate: ElasticsearchTemplate? = null

    @PostConstruct
    fun resetIndex() {
        var t = currentTimeMillis()
        elasticsearchTemplate!!.deleteIndex("_all")
        t = currentTimeMillis() - t
        logger.debug("Elasticsearch indexes reset in {} ms", t)
    }
}
