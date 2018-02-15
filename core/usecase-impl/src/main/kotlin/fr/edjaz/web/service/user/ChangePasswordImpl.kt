package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.model.User
import fr.edjaz.web.service.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChangePasswordImpl(    private val userGateway: UserGateway
                             , private val passwordEncoder: PasswordEncoder
                             , private val cacheManager: CacheManager): ChangePassword {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(password: String) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap<User>({ userGateway.findOneByLogin(it) })
            .ifPresent { user ->
                val encryptedPassword = passwordEncoder.encode(password)
                user.password = encryptedPassword
                userGateway.save(user);
                cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
                cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
                log.debug("Changed password for User: {}", user)
            }
    }
}
