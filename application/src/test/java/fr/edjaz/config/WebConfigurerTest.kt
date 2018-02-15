package fr.edjaz.config

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.servlet.InstrumentedFilter
import com.codahale.metrics.servlets.MetricsServlet
import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.web.filter.CachingHttpHeadersFilter
import io.undertow.Undertow
import io.undertow.Undertow.Builder
import io.undertow.UndertowOptions
import org.apache.commons.io.FilenameUtils
import org.h2.server.web.WebServlet
import org.junit.Before
import org.junit.Test
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory
import org.springframework.http.HttpHeaders
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.web.MockServletContext
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.xnio.OptionMap

import javax.servlet.*
import java.util.*

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Matchers.anyString
import org.mockito.Mockito.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

/**
 * Unit tests for the WebConfigurer class.
 *
 * @see WebConfigurer
 */
class WebConfigurerTest {

    private var webConfigurer: WebConfigurer? = null

    private var servletContext: MockServletContext? = null

    private var env: MockEnvironment? = null

    private var props: JHipsterProperties? = null

    private var metricRegistry: MetricRegistry? = null

    @Before
    fun setup() {
        servletContext = spy(MockServletContext())
        doReturn(MockFilterRegistration())
                .`when`<MockServletContext>(servletContext).addFilter(anyString(), any(Filter::class.java))
        doReturn(MockServletRegistration())
                .`when`<MockServletContext>(servletContext).addServlet(anyString(), any(Servlet::class.java))

        env = MockEnvironment()
        props = JHipsterProperties()

        webConfigurer = WebConfigurer(env!!, props!!)
        metricRegistry = MetricRegistry()
        webConfigurer!!.setMetricRegistry(metricRegistry!!)
    }

    @Test
    @Throws(ServletException::class)
    fun testStartUpProdServletContext() {
        env!!.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        webConfigurer!!.onStartup(servletContext!!)

        assertThat(servletContext!!.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE)).isEqualTo(metricRegistry)
        assertThat(servletContext!!.getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry)
        verify<MockServletContext>(servletContext).addFilter(Matchers.eq("webappMetricsFilter"), any(InstrumentedFilter::class.java))
        verify<MockServletContext>(servletContext).addServlet(Matchers.eq("metricsServlet"), any(MetricsServlet::class.java))
        verify<MockServletContext>(servletContext).addFilter(Matchers.eq("cachingHttpHeadersFilter"), any(CachingHttpHeadersFilter::class.java))
        verify<MockServletContext>(servletContext, never()).addServlet(Matchers.eq("H2Console"), any(WebServlet::class.java))
    }

    @Test
    @Throws(ServletException::class)
    fun testStartUpDevServletContext() {
        env!!.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
        webConfigurer!!.onStartup(servletContext!!)

        assertThat(servletContext!!.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE)).isEqualTo(metricRegistry)
        assertThat(servletContext!!.getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry)
        verify<MockServletContext>(servletContext).addFilter(Matchers.eq("webappMetricsFilter"), any(InstrumentedFilter::class.java))
        verify<MockServletContext>(servletContext).addServlet(Matchers.eq("metricsServlet"), any(MetricsServlet::class.java))
        verify<MockServletContext>(servletContext, never()).addFilter(Matchers.eq("cachingHttpHeadersFilter"), any(CachingHttpHeadersFilter::class.java))
        verify<MockServletContext>(servletContext).addServlet(Matchers.eq("H2Console"), any(WebServlet::class.java))
    }

    @Test
    fun testCustomizeServletContainer() {
        env!!.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        val container = UndertowEmbeddedServletContainerFactory()
        webConfigurer!!.customize(container)
        assertThat(container.mimeMappings.get("abs")).isEqualTo("audio/x-mpeg")
        assertThat(container.mimeMappings.get("html")).isEqualTo("text/html;charset=utf-8")
        assertThat(container.mimeMappings.get("json")).isEqualTo("text/html;charset=utf-8")
        if (container.documentRoot != null) {
            assertThat(container.documentRoot.path).isEqualTo(FilenameUtils.separatorsToSystem("build/www"))
        }

        val builder = Undertow.builder()
        container.builderCustomizers.forEach { c -> c.customize(builder) }
        val serverOptions = ReflectionTestUtils.getField(builder, "serverOptions") as OptionMap.Builder

        if(serverOptions.map.get(UndertowOptions.ENABLE_HTTP2) != null){
            throw RuntimeException("ENABLE_HTTP2 n'est pas null")
        }

    }

    @Test
    fun testUndertowHttp2Enabled() {
        props!!.http.setVersion(JHipsterProperties.Http.Version.V_2_0)
        val container = UndertowEmbeddedServletContainerFactory()
        webConfigurer!!.customize(container)
        val builder = Undertow.builder()
        container.builderCustomizers.forEach { c -> c.customize(builder) }
        val serverOptions = ReflectionTestUtils.getField(builder, "serverOptions") as OptionMap.Builder
        assertThat(serverOptions.map.get(UndertowOptions.ENABLE_HTTP2)).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterOnApiPath() {
        props!!.cors.allowedOrigins = listOf("*")
        props!!.cors.allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE")
        props!!.cors.allowedHeaders = listOf("*")
        props!!.cors.maxAge = 1800L
        props!!.cors.allowCredentials = true

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
                .addFilters<StandaloneMockMvcBuilder>(webConfigurer!!.corsFilter())
                .build()

        mockMvc.perform(
                options("/api/test-cors")
                        .header(HttpHeaders.ORIGIN, "other.domain.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"))
                .andExpect(header().string(HttpHeaders.VARY, "Origin"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1800"))

        mockMvc.perform(
                get("/api/test-cors")
                        .header(HttpHeaders.ORIGIN, "other.domain.com"))
                .andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"))
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterOnOtherPath() {
        props!!.cors.allowedOrigins = listOf("*")
        props!!.cors.allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE")
        props!!.cors.allowedHeaders = listOf("*")
        props!!.cors.maxAge = 1800L
        props!!.cors.allowCredentials = true

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
                .addFilters<StandaloneMockMvcBuilder>(webConfigurer!!.corsFilter())
                .build()

        mockMvc.perform(
                get("/test/test-cors")
                        .header(HttpHeaders.ORIGIN, "other.domain.com"))
                .andExpect(status().isOk)
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterDeactivated() {
        props!!.cors.allowedOrigins = null

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
                .addFilters<StandaloneMockMvcBuilder>(webConfigurer!!.corsFilter())
                .build()

        mockMvc.perform(
                get("/api/test-cors")
                        .header(HttpHeaders.ORIGIN, "other.domain.com"))
                .andExpect(status().isOk)
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    @Throws(Exception::class)
    fun testCorsFilterDeactivated2() {
        props!!.cors.allowedOrigins = ArrayList()

        val mockMvc = MockMvcBuilders.standaloneSetup(WebConfigurerTestController())
                .addFilters<StandaloneMockMvcBuilder>(webConfigurer!!.corsFilter())
                .build()

        mockMvc.perform(
                get("/api/test-cors")
                        .header(HttpHeaders.ORIGIN, "other.domain.com"))
                .andExpect(status().isOk)
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    internal class MockFilterRegistration : FilterRegistration, FilterRegistration.Dynamic {

        override fun addMappingForServletNames(dispatcherTypes: EnumSet<DispatcherType>, isMatchAfter: Boolean, vararg servletNames: String) {

        }

        override fun getServletNameMappings(): Collection<String>? {
            return null
        }

        override fun addMappingForUrlPatterns(dispatcherTypes: EnumSet<DispatcherType>, isMatchAfter: Boolean, vararg urlPatterns: String) {

        }

        override fun getUrlPatternMappings(): Collection<String>? {
            return null
        }

        override fun setAsyncSupported(isAsyncSupported: Boolean) {

        }

        override fun getName(): String? {
            return null
        }

        override fun getClassName(): String? {
            return null
        }

        override fun setInitParameter(name: String, value: String): Boolean {
            return false
        }

        override fun getInitParameter(name: String): String? {
            return null
        }

        override fun setInitParameters(initParameters: Map<String, String>): Set<String>? {
            return null
        }

        override fun getInitParameters(): Map<String, String>? {
            return null
        }
    }

    internal class MockServletRegistration : ServletRegistration, ServletRegistration.Dynamic {

        override fun setLoadOnStartup(loadOnStartup: Int) {

        }

        override fun setServletSecurity(constraint: ServletSecurityElement): Set<String>? {
            return null
        }

        override fun setMultipartConfig(multipartConfig: MultipartConfigElement) {

        }

        override fun setRunAsRole(roleName: String) {

        }

        override fun setAsyncSupported(isAsyncSupported: Boolean) {

        }

        override fun addMapping(vararg urlPatterns: String): Set<String>? {
            return null
        }

        override fun getMappings(): Collection<String>? {
            return null
        }

        override fun getRunAsRole(): String? {
            return null
        }

        override fun getName(): String? {
            return null
        }

        override fun getClassName(): String? {
            return null
        }

        override fun setInitParameter(name: String, value: String): Boolean {
            return false
        }

        override fun getInitParameter(name: String): String? {
            return null
        }

        override fun setInitParameters(initParameters: Map<String, String>): Set<String>? {
            return null
        }

        override fun getInitParameters(): Map<String, String>? {
            return null
        }
    }
}
