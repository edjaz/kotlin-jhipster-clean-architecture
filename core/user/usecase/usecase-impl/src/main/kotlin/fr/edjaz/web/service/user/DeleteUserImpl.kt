package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DeleteUserImpl(    private val userGateway: UserGateway
                         , private val userSearchGateway: UserSearchGateway
                         , private val cacheManager: CacheManager
) : DeleteUser {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(login: String) {
        userGateway.findOneByLogin(login).ifPresent { user ->
            //socialService.deleteUserSocialConnection(user.login!!)
            userGateway.delete(user)
            userSearchGateway.delete(user)
            cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
            cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
            log.debug("Deleted User: {}", user)
        }
    }
}
