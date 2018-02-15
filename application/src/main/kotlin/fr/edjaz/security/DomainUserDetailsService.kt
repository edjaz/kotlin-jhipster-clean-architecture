package fr.edjaz.security

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.model.User
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import java.util.*
import java.util.stream.Collectors

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
class DomainUserDetailsService(private val userGateway: UserGateway) : UserDetailsService {

    private val log = LoggerFactory.getLogger(DomainUserDetailsService::class.java)

    @Transactional
    override fun loadUserByUsername(login: String): UserDetails {
        log.debug("Authenticating {}", login)
        val lowercaseLogin = login.toLowerCase(Locale.ENGLISH)
        val userByEmailFromDatabase = userGateway.findOneWithAuthoritiesByEmail(lowercaseLogin)
        return userByEmailFromDatabase.map { user -> createSpringSecurityUser(lowercaseLogin, user) }.orElseGet {
            val userByLoginFromDatabase = userGateway.findOneWithAuthoritiesByLogin(lowercaseLogin)
            userByLoginFromDatabase.map { user -> createSpringSecurityUser(lowercaseLogin, user) }
                    .orElseThrow {
                        UsernameNotFoundException("User " + lowercaseLogin + " was not found in the " +
                                "database")
                    }
        }
    }

    private fun createSpringSecurityUser(lowercaseLogin: String, user: User): org.springframework.security.core.userdetails.User {
        if (!user.activated) {
            throw UserNotActivatedException("User $lowercaseLogin was not activated")
        }
        val grantedAuthorities = user.authorities.stream()
                .map { authority -> SimpleGrantedAuthority(authority.name) }
                .collect(Collectors.toList())
        return org.springframework.security.core.userdetails.User(user.login!!,
                user.password!!,
                grantedAuthorities)
    }
}
