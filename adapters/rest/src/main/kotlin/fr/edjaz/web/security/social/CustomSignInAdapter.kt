package fr.edjaz.web.security.social

import fr.edjaz.web.security.jwt.TokenProvider

import io.github.jhipster.config.JHipsterProperties

import org.slf4j.LoggerFactory

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.social.connect.Connection
import org.springframework.social.connect.web.SignInAdapter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.security.core.AuthenticationException
import org.springframework.web.context.request.ServletWebRequest
import javax.servlet.http.Cookie

class CustomSignInAdapter(private val userDetailsService: UserDetailsService, private val jHipsterProperties: JHipsterProperties,
                          private val tokenProvider: TokenProvider) : SignInAdapter {

    private val log = LoggerFactory.getLogger(CustomSignInAdapter::class.java)

    override fun signIn(userId: String, connection: Connection<*>, request: NativeWebRequest): String {
        try {
            val user = userDetailsService.loadUserByUsername(userId)
            val authenticationToken = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.authorities)

            SecurityContextHolder.getContext().authentication = authenticationToken
            val jwt = tokenProvider.createToken(authenticationToken, false)
            val servletWebRequest = request as ServletWebRequest
            servletWebRequest.response.addCookie(getSocialAuthenticationCookie(jwt))
        } catch (ae: AuthenticationException) {
            log.error("Social authentication error")
            log.trace("Authentication exception trace: {}", ae)
        }

        return jHipsterProperties.social.redirectAfterSignIn
    }

    private fun getSocialAuthenticationCookie(token: String): Cookie {
        val socialAuthCookie = Cookie("social-authentication", token)
        socialAuthCookie.path = "/"
        socialAuthCookie.maxAge = 10
        return socialAuthCookie
    }
}
