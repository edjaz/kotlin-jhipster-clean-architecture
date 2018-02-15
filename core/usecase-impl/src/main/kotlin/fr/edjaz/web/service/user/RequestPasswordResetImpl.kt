package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.web.service.user.response.UserModelResponse
import fr.edjaz.web.service.util.RandomUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional
class RequestPasswordResetImpl(    private val userGateway: UserGateway
                                   , private val cacheManager: CacheManager
) : RequestPasswordReset {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(mail: String): Optional<UserModelResponse> {
        return userGateway.findOneByEmailIgnoreCase(mail)
            .filter({ it.activated })
            .map { user ->
                user.resetKey = RandomUtil.generateResetKey()
                user.resetDate = Instant.now()
                userGateway.save(user)
                cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
                cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
                user.toResponse()
            }
    }
}
