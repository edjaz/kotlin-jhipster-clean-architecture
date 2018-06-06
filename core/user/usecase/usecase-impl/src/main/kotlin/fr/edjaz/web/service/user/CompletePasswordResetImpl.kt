package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.web.service.user.request.CompletePasswordResetRequest
import fr.edjaz.web.service.user.response.UserModelResponse
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*


@Service
@Transactional
class CompletePasswordResetImpl(    private val userGateway: UserGateway
                                   , private val passwordEncoder: PasswordEncoder
                                   , private val cacheManager: CacheManager) : CompletePasswordReset {


    private val log = LoggerFactory.getLogger(this::class.java)


    override fun execute(request: CompletePasswordResetRequest): Optional<UserModelResponse> {
        log.debug("Reset user password for reset key {}", request)

        return userGateway.findOneByResetKey(request.key)
            .filter { user -> user.resetDate!!.isAfter(Instant.now().minusSeconds(86400)) }
            .map { user ->
                user.password = passwordEncoder.encode(request.newPassword)
                user.resetKey = null
                user.resetDate = null

                userGateway.save(user)

                cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
                cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
                user.toResponse()
            }
    }



}


