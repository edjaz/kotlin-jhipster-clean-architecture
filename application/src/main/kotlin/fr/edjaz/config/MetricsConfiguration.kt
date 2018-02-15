package fr.edjaz.config

import io.github.jhipster.config.JHipsterProperties

import com.codahale.metrics.JmxReporter
import com.codahale.metrics.JvmAttributeGaugeSet
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.jcache.JCacheGaugeSet
import com.codahale.metrics.jvm.*
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*

import javax.annotation.PostConstruct
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

@Configuration
@EnableMetrics(proxyTargetClass = true)
class MetricsConfiguration(private val jHipsterProperties: JHipsterProperties) : MetricsConfigurerAdapter() {

    private val log = LoggerFactory.getLogger(MetricsConfiguration::class.java)

    private val metricRegistry = MetricRegistry()

    private val healthCheckRegistry = HealthCheckRegistry()

    private var hikariDataSource: HikariDataSource? = null

    @Autowired(required = false)
    fun setHikariDataSource(hikariDataSource: HikariDataSource) {
        this.hikariDataSource = hikariDataSource
    }

    @Bean
    override fun getMetricRegistry(): MetricRegistry {
        return metricRegistry
    }

    @Bean
    override fun getHealthCheckRegistry(): HealthCheckRegistry {
        return healthCheckRegistry
    }

    @PostConstruct
    fun init() {
        log.debug("Registering JVM gauges")
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, MemoryUsageGaugeSet())
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, GarbageCollectorMetricSet())
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, ThreadStatesGaugeSet())
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, FileDescriptorRatioGauge())
        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS, BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()))
        metricRegistry.register(PROP_METRIC_REG_JVM_ATTRIBUTE_SET, JvmAttributeGaugeSet())
        metricRegistry.register(PROP_METRIC_REG_JCACHE_STATISTICS, JCacheGaugeSet())
        if (hikariDataSource != null) {
            log.debug("Monitoring the datasource")
            hikariDataSource!!.metricRegistry = metricRegistry
        }
        if (jHipsterProperties.metrics.jmx.isEnabled) {
            log.debug("Initializing Metrics JMX reporting")
            val jmxReporter = JmxReporter.forRegistry(metricRegistry).build()
            jmxReporter.start()
        }
        if (jHipsterProperties.metrics.logs.isEnabled) {
            log.info("Initializing Metrics Log reporting")
            val metricsMarker = MarkerFactory.getMarker("metrics")
            val reporter = Slf4jReporter.forRegistry(metricRegistry)
                    .outputTo(LoggerFactory.getLogger("metrics"))
                    .markWith(metricsMarker)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build()
            reporter.start(jHipsterProperties.metrics.logs.reportFrequency, TimeUnit.SECONDS)
        }
    }

    companion object {

        private val PROP_METRIC_REG_JVM_MEMORY = "jvm.memory"
        private val PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage"
        private val PROP_METRIC_REG_JVM_THREADS = "jvm.threads"
        private val PROP_METRIC_REG_JVM_FILES = "jvm.files"
        private val PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers"
        private val PROP_METRIC_REG_JVM_ATTRIBUTE_SET = "jvm.attributes"

        private val PROP_METRIC_REG_JCACHE_STATISTICS = "jcache.statistics"
    }
}
