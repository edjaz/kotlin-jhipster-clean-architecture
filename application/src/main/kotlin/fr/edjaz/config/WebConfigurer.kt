package fr.edjaz.config

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.servlet.InstrumentedFilter
import com.codahale.metrics.servlets.MetricsServlet
import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.web.filter.CachingHttpHeadersFilter
import io.undertow.UndertowOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.MimeMappings
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.Servlet
import javax.servlet.ServletContext
import javax.servlet.ServletException

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
class WebConfigurer(private val env: Environment, private val jHipsterProperties: JHipsterProperties) : ServletContextInitializer, EmbeddedServletContainerCustomizer {

    private val log = LoggerFactory.getLogger(WebConfigurer::class.java)

    private var metricRegistry: MetricRegistry? = null

    @Throws(ServletException::class)
    override fun onStartup(servletContext: ServletContext) {
        if (env.activeProfiles.isNotEmpty()) {
            log.info("Web application configuration, using profiles: {}", env.activeProfiles)
        }
        val disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC)
        initMetrics(servletContext, disps)
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            initCachingHttpHeadersFilter(servletContext, disps)
        }
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)) {
            initH2Console(servletContext)
        }
        log.info("Web application fully configured")
    }

    /**
     * Customize the Servlet engine: Mime types, the document root, the cache.
     */
    override fun customize(container: ConfigurableEmbeddedServletContainer) {
        val mappings = MimeMappings(MimeMappings.DEFAULT)
        // IE issue, see https://github.com/jhipster/generator-jhipster/pull/711
        mappings.add("html", MediaType.TEXT_HTML_VALUE + ";charset=utf-8")
        // CloudFoundry issue, see https://github.com/cloudfoundry/gorouter/issues/64
        mappings.add("json", MediaType.TEXT_HTML_VALUE + ";charset=utf-8")
        container.setMimeMappings(mappings)
        // When running in an IDE or with ./gradlew bootRun, set location of the static web assets.
        setLocationForStaticAssets(container)

        /*
         * Enable HTTP/2 for Undertow - https://twitter.com/ankinson/status/829256167700492288
         * HTTP/2 requires HTTPS, so HTTP requests will fallback to HTTP/1.1.
         * See the JHipsterProperties class and your application-*.yml configuration files
         * for more information.
         */



        if (jHipsterProperties.http.getVersion() == JHipsterProperties.Http.Version.V_2_0 && container is UndertowEmbeddedServletContainerFactory) {
            container
                .addBuilderCustomizers(UndertowBuilderCustomizer { it.setServerOption(UndertowOptions.ENABLE_HTTP2, true) })

        }
    }

    private fun setLocationForStaticAssets(container: ConfigurableEmbeddedServletContainer) {
        val root: File
        val prefixPath = resolvePathPrefix()
        root = File(prefixPath + "build/www/")
        if (root.exists() && root.isDirectory) {
            container.setDocumentRoot(root)
        }
    }

    /**
     * Resolve path prefix to static resources.
     */
    private fun resolvePathPrefix(): String {
        val fullExecutablePath = this.javaClass.getResource("").path
        val rootPath = Paths.get(".").toUri().normalize().path
        val extractedPath = fullExecutablePath.replace(rootPath, "")
        val extractionEndIndex = extractedPath.indexOf("build/")
        return if (extractionEndIndex <= 0) {
            ""
        } else extractedPath.substring(0, extractionEndIndex)
    }

    /**
     * Initializes the caching HTTP Headers Filter.
     */
    private fun initCachingHttpHeadersFilter(servletContext: ServletContext,
                                             disps: EnumSet<DispatcherType>) {
        log.debug("Registering Caching HTTP Headers Filter")
        val cachingHttpHeadersFilter = servletContext.addFilter("cachingHttpHeadersFilter",
                CachingHttpHeadersFilter(jHipsterProperties))

        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/content/*")
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/app/*")
        cachingHttpHeadersFilter.setAsyncSupported(true)
    }

    /**
     * Initializes Metrics.
     */
    private fun initMetrics(servletContext: ServletContext, disps: EnumSet<DispatcherType>) {
        log.debug("Initializing Metrics registries")
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE,
                metricRegistry)
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
                metricRegistry)

        log.debug("Registering Metrics Filter")
        val metricsFilter = servletContext.addFilter("webappMetricsFilter",
                InstrumentedFilter())

        metricsFilter.addMappingForUrlPatterns(disps, true, "/*")
        metricsFilter.setAsyncSupported(true)

        log.debug("Registering Metrics Servlet")
        val metricsAdminServlet = servletContext.addServlet("metricsServlet", MetricsServlet())

        metricsAdminServlet.addMapping("/management/metrics/*")
        metricsAdminServlet.setAsyncSupported(true)
        metricsAdminServlet.setLoadOnStartup(2)
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = jHipsterProperties.cors
        if (config.allowedOrigins != null && !config.allowedOrigins.isEmpty()) {
            log.debug("Registering CORS filter")
            source.registerCorsConfiguration("/api/**", config)
            source.registerCorsConfiguration("/management/**", config)
            source.registerCorsConfiguration("/v2/api-docs", config)
        }
        return CorsFilter(source)
    }

    /**
     * Initializes H2 console.
     */
    private fun initH2Console(servletContext: ServletContext) {
        log.debug("Initialize H2 console")
        try {
            // We don't want to include H2 when we are packaging for the "prod" profile and won't
            // actually need it, so we have to load / invoke things at runtime through reflection.
            val loader = Thread.currentThread().contextClassLoader
            val servletClass = Class.forName("org.h2.server.web.WebServlet", true, loader)
            val servlet = servletClass.newInstance() as Servlet

            val h2ConsoleServlet = servletContext.addServlet("H2Console", servlet)
            h2ConsoleServlet.addMapping("/h2-console/*")
            h2ConsoleServlet.setInitParameter("-properties", "src/main/resources/")
            h2ConsoleServlet.setLoadOnStartup(1)

        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Failed to load and initialize org.h2.server.web.WebServlet", e)

        } catch (e: LinkageError) {
            throw RuntimeException("Failed to load and initialize org.h2.server.web.WebServlet", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Failed to instantiate org.h2.server.web.WebServlet", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("Failed to instantiate org.h2.server.web.WebServlet", e)
        }

    }

    @Autowired(required = false)
    fun setMetricRegistry(metricRegistry: MetricRegistry) {
        this.metricRegistry = metricRegistry
    }
}
