package fr.edjaz.web.service.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

/**
 * Utility class for Spring Security.
 */
object SecurityUtils {

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user
     */
    fun getCurrentUserLogin(): Optional<String> {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable<Authentication>(securityContext.authentication)
                .map { authentication ->
                    if (authentication.getPrincipal() is UserDetails) {
                        val springSecurityUser = authentication.getPrincipal() as UserDetails
                        springSecurityUser.username
                    } else if (authentication.getPrincipal() is String) {
                        authentication.principal as String
                    }else {
                        null
                    }
                }
        }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user
     */
    fun getCurrentUserJWT(): Optional<String> {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable<Authentication>(securityContext.authentication)
                .filter { authentication -> authentication.getCredentials() is String }
                .map { authentication -> authentication.getCredentials() as String }
        }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    fun isAuthenticated(): Boolean {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable<Authentication>(securityContext.authentication)
                .map<Boolean> { authentication ->
                    authentication.getAuthorities().stream()
                        .noneMatch({ grantedAuthority -> grantedAuthority.getAuthority() == AuthoritiesConstants.ANONYMOUS })
                }
                .orElse(false)
        }

    /**
     * If the current user has a specific authority (security role).
     *
     *
     * The name of this method comes from the isUserInRole() method in the Servlet API
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    fun isCurrentUserInRole(authority: String): Boolean {
        val securityContext = SecurityContextHolder.getContext()
        return Optional.ofNullable<Authentication>(securityContext.authentication)
            .map<Boolean> { authentication ->
                authentication.getAuthorities().stream()
                    .anyMatch({ grantedAuthority -> grantedAuthority.getAuthority() == authority })
            }
            .orElse(false)
    }
}
