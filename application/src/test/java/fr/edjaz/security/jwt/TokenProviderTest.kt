package fr.edjaz.security.jwt

import fr.edjaz.web.security.jwt.TokenProvider
import fr.edjaz.web.service.security.AuthoritiesConstants
import io.github.jhipster.config.JHipsterProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.util.ReflectionTestUtils

import java.util.ArrayList
import java.util.Date

import org.assertj.core.api.Assertions.assertThat

class TokenProviderTest {

    private val secretKey = "e5c9ee274ae87bc031adda32e27fa98b9290da83"
    private val ONE_MINUTE: Long = 60000
    private var jHipsterProperties: JHipsterProperties? = null
    private var tokenProvider: TokenProvider? = null

    @Before
    fun setup() {
        jHipsterProperties = Mockito.mock(JHipsterProperties::class.java)
        tokenProvider = TokenProvider(jHipsterProperties!!)
        ReflectionTestUtils.setField(tokenProvider, "secretKey", secretKey)
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", ONE_MINUTE)
    }

    @Test
    fun testReturnFalseWhenJWThasInvalidSignature() {
        val isTokenValid = tokenProvider!!.validateToken(createTokenWithDifferentSignature())

        assertThat(isTokenValid).isEqualTo(false)
    }

    @Test
    fun testReturnFalseWhenJWTisMalformed() {
        val authentication = createAuthentication()
        val token = tokenProvider!!.createToken(authentication, false)
        val invalidToken = token.substring(1)
        val isTokenValid = tokenProvider!!.validateToken(invalidToken)

        assertThat(isTokenValid).isEqualTo(false)
    }

    @Test
    fun testReturnFalseWhenJWTisExpired() {
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", -ONE_MINUTE)

        val authentication = createAuthentication()
        val token = tokenProvider!!.createToken(authentication, false)

        val isTokenValid = tokenProvider!!.validateToken(token)

        assertThat(isTokenValid).isEqualTo(false)
    }

    @Test
    fun testReturnFalseWhenJWTisUnsupported() {
        val unsupportedToken = createUnsupportedToken()

        val isTokenValid = tokenProvider!!.validateToken(unsupportedToken)

        assertThat(isTokenValid).isEqualTo(false)
    }

    @Test
    fun testReturnFalseWhenJWTisInvalid() {
        val isTokenValid = tokenProvider!!.validateToken("")

        assertThat(isTokenValid).isEqualTo(false)
    }

    private fun createAuthentication(): Authentication {
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS))
        return UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities)
    }

    private fun createUnsupportedToken(): String {
        return Jwts.builder()
                .setPayload("payload")
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact()
    }

    private fun createTokenWithDifferentSignature(): String {
        return Jwts.builder()
                .setSubject("anonymous")
                .signWith(SignatureAlgorithm.HS512, "e5c9ee274ae87bc031adda32e27fa98b9290da90")
                .setExpiration(Date(Date().time + ONE_MINUTE))
                .compact()
    }
}
