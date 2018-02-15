package fr.edjaz.security

import fr.edjaz.web.service.security.SecurityUtils
import fr.edjaz.web.service.security.AuthoritiesConstants
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

import java.util.ArrayList

import org.assertj.core.api.Assertions.assertThat

/**
 * Test class for the SecurityUtils utility class.
 *
 * @see SecurityUtils
 */
class SecurityUtilsUnitTest {

    @Test
    fun testgetCurrentUserLogin() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val login = SecurityUtils.getCurrentUserLogin()
        assertThat(login).contains("admin")
    }

    @Test
    fun testgetCurrentUserJWT() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "token")
        SecurityContextHolder.setContext(securityContext)
        val jwt = SecurityUtils.getCurrentUserJWT()
        assertThat(jwt).contains("token")
    }

    @Test
    fun testIsAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = SecurityUtils.isAuthenticated()
        assertThat(isAuthenticated).isTrue()
    }

    @Test
    fun testAnonymousIsNotAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS))
        securityContext.authentication = UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities)
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = SecurityUtils.isAuthenticated()
        assertThat(isAuthenticated).isFalse()
    }

    @Test
    fun testIsCurrentUserInRole() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority(AuthoritiesConstants.USER))
        securityContext.authentication = UsernamePasswordAuthenticationToken("user", "user", authorities)
        SecurityContextHolder.setContext(securityContext)

        assertThat(SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.USER)).isTrue()
        assertThat(SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)).isFalse()
    }

}
