package fr.edjaz.security.jwt

import fr.edjaz.web.security.jwt.JWTConfigurer
import fr.edjaz.web.security.jwt.JWTFilter
import fr.edjaz.web.security.jwt.TokenProvider
import fr.edjaz.web.service.security.AuthoritiesConstants
import io.github.jhipster.config.JHipsterProperties
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.util.ReflectionTestUtils

import org.assertj.core.api.Assertions.assertThat

class JWTFilterTest {

    private var tokenProvider: TokenProvider? = null

    private var jwtFilter: JWTFilter? = null

    @Before
    fun setup() {
        val jHipsterProperties = JHipsterProperties()
        tokenProvider = TokenProvider(jHipsterProperties)
        ReflectionTestUtils.setField(tokenProvider, "secretKey", "test secret")
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", 60000)
        jwtFilter = JWTFilter(tokenProvider!!)
        SecurityContextHolder.getContext().authentication = null
    }

    @Test
    @Throws(Exception::class)
    fun testJWTFilter() {
        val authentication = UsernamePasswordAuthenticationToken(
                "test-user",
                "test-password",
                listOf(SimpleGrantedAuthority(AuthoritiesConstants.USER))
        )
        val jwt = tokenProvider!!.createToken(authentication, false)
        val request = MockHttpServletRequest()
        request.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt)
        request.requestURI = "/api/test"
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()
        jwtFilter!!.doFilter(request, response, filterChain)
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())
        assertThat(SecurityContextHolder.getContext().authentication.name).isEqualTo("test-user")
        assertThat(SecurityContextHolder.getContext().authentication.credentials.toString()).isEqualTo(jwt)
    }

    @Test
    @Throws(Exception::class)
    fun testJWTFilterInvalidToken() {
        val jwt = "wrong_jwt"
        val request = MockHttpServletRequest()
        request.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt)
        request.requestURI = "/api/test"
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()
        jwtFilter!!.doFilter(request, response, filterChain)
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testJWTFilterMissingAuthorization() {
        val request = MockHttpServletRequest()
        request.requestURI = "/api/test"
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()
        jwtFilter!!.doFilter(request, response, filterChain)
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testJWTFilterMissingToken() {
        val request = MockHttpServletRequest()
        request.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer ")
        request.requestURI = "/api/test"
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()
        jwtFilter!!.doFilter(request, response, filterChain)
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testJWTFilterWrongScheme() {
        val authentication = UsernamePasswordAuthenticationToken(
                "test-user",
                "test-password",
                listOf(SimpleGrantedAuthority(AuthoritiesConstants.USER))
        )
        val jwt = tokenProvider!!.createToken(authentication, false)
        val request = MockHttpServletRequest()
        request.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Basic " + jwt)
        request.requestURI = "/api/test"
    }

}
