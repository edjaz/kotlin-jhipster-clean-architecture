package fr.edjaz.config

import io.github.jhipster.config.JHipsterProperties
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.expiry.Duration
import org.ehcache.expiry.Expirations
import org.ehcache.jsr107.Eh107Configuration

import java.util.concurrent.TimeUnit

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.*

@Configuration
@EnableCaching
@AutoConfigureAfter(value = [MetricsConfiguration::class])
@AutoConfigureBefore(value = [WebConfigurer::class, DatabaseConfiguration::class])
class CacheConfiguration(jHipsterProperties: JHipsterProperties) {

    private val jcacheConfiguration: javax.cache.configuration.Configuration<Any, Any>

    init {
        val ehcache = jHipsterProperties.cache.ehcache

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Any::class.java, Any::class.java,
                        ResourcePoolsBuilder.heap(ehcache.maxEntries))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(ehcache.timeToLiveSeconds.toLong(), TimeUnit.SECONDS)))
                        .build())
    }

    @Bean
    fun cacheManagerCustomizer(): JCacheManagerCustomizer {
        return JCacheManagerCustomizer {
            it.createCache(fr.edjaz.domain.gateway.UserGateway.USERS_BY_LOGIN_CACHE, jcacheConfiguration)
            it.createCache(fr.edjaz.domain.gateway.UserGateway.USERS_BY_EMAIL_CACHE, jcacheConfiguration)
            it.createCache(fr.edjaz.domain.UserEntity::class.java.name, jcacheConfiguration)
            it.createCache(fr.edjaz.domain.AuthorityEntity::class.java.name, jcacheConfiguration)
            it.createCache(fr.edjaz.domain.UserEntity::class.java.name + ".authorities", jcacheConfiguration)
            it.createCache(fr.edjaz.domain.SocialUserConnectionEntity::class.java.name, jcacheConfiguration)
        }
    }
}
