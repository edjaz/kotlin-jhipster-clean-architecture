package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import fr.edjaz.web.service.user.response.UserModelResponse
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class ActivateRegistrationImpl(    private val userGateway: UserGateway
                                   , private val userSearchGateway: UserSearchGateway
                                   , private val cacheManager: CacheManager) : ActivateRegistration {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(key: String): Optional<UserModelResponse> {
        log.debug("Activating user for activation key {}", key)
        return userGateway.findOneByActivationKey(key)
            .map { user ->

                val response = user.toResponse()
                // activate given user for the registration key.
                response.activated = true
                response.activationKey = null

                user.activated = true
                user.activationKey = null

                userGateway.save(user);
                userSearchGateway.save(user)
                cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
                cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
                log.debug("Activated user: {}", user)
                response
            }
    }
}
