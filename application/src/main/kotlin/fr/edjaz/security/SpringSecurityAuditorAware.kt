package fr.edjaz.security


import fr.edjaz.domain.config.Constants
import fr.edjaz.web.service.security.SecurityUtils

import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): String {
        return SecurityUtils.getCurrentUserLogin().orElse(Constants.SYSTEM_ACCOUNT)
    }
}
