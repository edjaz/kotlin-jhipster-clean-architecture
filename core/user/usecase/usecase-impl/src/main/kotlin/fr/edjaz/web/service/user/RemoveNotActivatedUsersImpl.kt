package fr.edjaz.web.service.user

import fr.edjaz.domain.gateway.UserGateway
import fr.edjaz.domain.gateway.UserSearchGateway
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class RemoveNotActivatedUsersImpl(
    private val userGateway: UserGateway
    , private val userSearchGateway: UserSearchGateway
    , private val cacheManager: CacheManager
) : RemoveNotActivatedUsers {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "0 0 1 * * ?")
    override fun execute() {
        val users = userGateway.findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
        for (user in users) {
            log.debug("Deleting not activated user {}", user.login)
            userGateway.delete(user)
            userSearchGateway.delete(user)
            cacheManager.getCache(UserGateway.USERS_BY_LOGIN_CACHE).evict(user.login)
            cacheManager.getCache(UserGateway.USERS_BY_EMAIL_CACHE).evict(user.email)
        }
    }
}
