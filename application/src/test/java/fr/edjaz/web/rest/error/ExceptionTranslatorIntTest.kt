package fr.edjaz.web.rest.error

import fr.edjaz.AppApp
import fr.edjaz.web.service.rest.errors.ErrorConstants
import fr.edjaz.web.rest.errors.ExceptionTranslator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.zalando.problem.spring.web.advice.MediaTypes

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Test class for the ExceptionTranslator controller advice.
 *
 * @see ExceptionTranslator
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AppApp::class))
class ExceptionTranslatorIntTest {

    @Autowired
    private val controller: ExceptionTranslatorTestController? = null

    @Autowired
    private val exceptionTranslator: ExceptionTranslator? = null

    @Autowired
    private val jacksonMessageConverter: MappingJackson2HttpMessageConverter? = null

    private var mockMvc: MockMvc? = null

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionTranslator!!)
                .setMessageConverters(jacksonMessageConverter!!)
                .build()
    }

    @Test
    @Throws(Exception::class)
    fun testConcurrencyFailure() {
        mockMvc!!.perform(get("/test/concurrency-failure"))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value(ErrorConstants.ERR_CONCURRENCY_FAILURE))
    }

    @Test
    @Throws(Exception::class)
    fun testMethodArgumentNotValid() {
        mockMvc!!.perform(post("/test/method-argument").content("{}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value(ErrorConstants.ERR_VALIDATION))
                .andExpect(jsonPath("$.fieldErrors.[0].objectName").value("testDTO"))
                .andExpect(jsonPath("$.fieldErrors.[0].field").value("test"))
                .andExpect(jsonPath("$.fieldErrors.[0].message").value("NotNull"))
    }

    @Test
    @Throws(Exception::class)
    fun testParameterizedError() {
        mockMvc!!.perform(get("/test/parameterized-error"))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("test parameterized error"))
                .andExpect(jsonPath("$.params.param0").value("param0_value"))
                .andExpect(jsonPath("$.params.param1").value("param1_value"))
    }

    @Test
    @Throws(Exception::class)
    fun testParameterizedError2() {
        mockMvc!!.perform(get("/test/parameterized-error2"))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("test parameterized error"))
                .andExpect(jsonPath("$.params.foo").value("foo_value"))
                .andExpect(jsonPath("$.params.bar").value("bar_value"))
    }

    @Test
    @Throws(Exception::class)
    fun testMissingServletRequestPartException() {
        mockMvc!!.perform(get("/test/missing-servlet-request-part"))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.400"))
    }

    @Test
    @Throws(Exception::class)
    fun testMissingServletRequestParameterException() {
        mockMvc!!.perform(get("/test/missing-servlet-request-parameter"))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.400"))
    }

    @Test
    @Throws(Exception::class)
    fun testAccessDenied() {
        mockMvc!!.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.403"))
                .andExpect(jsonPath("$.detail").value("test access denied!"))
    }

    @Test
    @Throws(Exception::class)
    fun testUnauthorized() {
        mockMvc!!.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.401"))
                .andExpect(jsonPath("$.path").value("/test/unauthorized"))
                .andExpect(jsonPath("$.detail").value("test authentication failed!"))
    }

    @Test
    @Throws(Exception::class)
    fun testMethodNotSupported() {
        mockMvc!!.perform(post("/test/access-denied"))
                .andExpect(status().isMethodNotAllowed)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.405"))
                .andExpect(jsonPath("$.detail").value("Request method 'POST' not supported"))
    }

    @Test
    @Throws(Exception::class)
    fun testExceptionWithResponseStatus() {
        mockMvc!!.perform(get("/test/response-status"))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.400"))
                .andExpect(jsonPath("$.title").value("test response status"))
    }

    @Test
    @Throws(Exception::class)
    fun testInternalServerError() {
        mockMvc!!.perform(get("/test/internal-server-error"))
                .andExpect(status().isInternalServerError)
                .andExpect(content().contentType(MediaTypes.PROBLEM))
                .andExpect(jsonPath("$.message").value("error.http.500"))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
    }

}
